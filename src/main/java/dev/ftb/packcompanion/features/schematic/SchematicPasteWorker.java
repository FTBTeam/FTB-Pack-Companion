package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SchematicPasteWorker {
    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private final ResourceLocation location;
    private final BlockPos basePos;
    private final int blocksPerTick;  // blocks per tick
    private final BlockPos.MutableBlockPos currentPos;
    @Nullable
    private final CommandSourceStack sourceStack;
    private ResourceLocation dimensionId;
    private ServerLevel level;
    private SchematicData data;
    private State state;
    private CompletableFuture<Void> future;
    private String terminationMessage = "";

    public SchematicPasteWorker(@Nullable CommandSourceStack sourceStack, ResourceLocation location, Either<ServerLevel,ResourceLocation> levelOrDimensionId, BlockPos basePos, int blocksPerTick) {
        this.sourceStack = sourceStack;
        this.location = location;
        this.basePos = basePos;
        this.blocksPerTick = blocksPerTick;
        levelOrDimensionId
                .ifLeft(level -> {
                    this.level = level;
                    this.dimensionId = level.dimension().location();
                })
                .ifRight(dimId -> this.dimensionId = dimId);
        state = State.INIT;
        currentPos = BlockPos.ZERO.mutable();
    }

    public static Optional<SchematicPasteWorker> fromNBT(Tag tag) {
        if (tag instanceof CompoundTag c) {
            SchematicPasteWorker worker = new SchematicPasteWorker(
                    null,
                    ResourceLocation.tryParse(c.getString("schematic")),
                    Either.right(ResourceLocation.tryParse(c.getString("dimensionId"))),
                    NbtUtils.readBlockPos(c.getCompound("basePos")),
                    c.getInt("speed")
            );
            worker.currentPos.set(NbtUtils.readBlockPos(c.getCompound("currentPos")).mutable());
            return Optional.of(worker);
        }
        return Optional.empty();
    }

    public Tag saveNBT() {
        return Util.make(new CompoundTag(), tag -> {
            tag.putString("schematic", location.toString());
            tag.putString("dimensionId", dimensionId.toString());
            tag.put("basePos", NbtUtils.writeBlockPos(basePos));
            tag.putInt("speed", blocksPerTick);
            tag.put("currentPos", NbtUtils.writeBlockPos(currentPos));
        });
    }

    public void tick(MinecraftServer server) {
        if (state == State.INIT) {
            if (level == null) {
                level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
                if (level == null) {
                    SchematicPasteManager.LOGGER.error("unknown level {}", dimensionId);
                    fail("Invalid level %s", dimensionId);
                    return;
                }
            }
            state = State.LOADING;
            loadSchematicDataAsync(server);
        } else if (state == State.PASTING) {
            for (int i = 0; i < blocksPerTick; i++) {
                BlockPos destPos = basePos.offset(currentPos);
                level.setBlock(destPos, data.getBlockAt(currentPos, AIR_STATE), Block.UPDATE_CLIENTS);
                data.getBlockEntityDataAt(currentPos).ifPresent(beData -> {
                    BlockEntity be = level.getBlockEntity(destPos);
                    if (be != null) {
                        be.load(beData);
                    }
                });
                advanceCurrentPos();
                if (state == State.FINISHED) return;
            }
        }
    }

    private void advanceCurrentPos() {
        currentPos.move(Direction.EAST); // pos X
        if (currentPos.getX() >= data.getWidth()) {
            currentPos.setX(0);
            currentPos.move(Direction.SOUTH); // pos Z
            if (currentPos.getZ() >= data.getLength()) {
                currentPos.setZ(0);
                currentPos.move(Direction.UP); // pos Y
                if (currentPos.getY() >= data.getHeight() || currentPos.getY() >= level.getMaxBuildHeight()) {
                    state = State.FINISHED;
                }
            }
        }
    }

    private void loadSchematicDataAsync(MinecraftServer server) {
        var fullLoc = location.withPath(p -> "schematics/" + p + ".schem");
        future = CompletableFuture.runAsync(() -> server.getResourceManager().getResource(fullLoc).ifPresentOrElse(resource -> {
            try (var in = resource.open()) {
                CompoundTag schemTag = NbtIo.readCompressed(in);
                data = SchematicData.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), schemTag);
                state = State.PASTING;
            } catch (IOException e) {
                SchematicPasteManager.LOGGER.error("can't open resource {}: {}", location, e.getMessage());
                fail("Schematic %s can't be read: %s", location, e.getMessage());
            }
        }, () -> {
            SchematicPasteManager.LOGGER.error("unknown resource {}", location);
            fail("Resource %s does not exist in resource manager", location);
        }));
    }

    public State getState() {
        return state;
    }

    public ResourceLocation getDimensionId() {
        return dimensionId;
    }

    public ResourceLocation makeKey() {
        return location.withSuffix("_" + basePos.getX() + "_" + basePos.getY() + "_" + basePos.getZ());
    }

    public boolean isDone() {
        return !state.running;
    }

    public int getProgress() {
        if (data == null) return 0;
        int size = data.getWidth() * data.getHeight() * data.getLength();
        int c = currentPos.getX() + currentPos.getZ() * data.getWidth() + currentPos.getY() * data.getWidth() * data.getHeight();

        return c * 100 / size;
    }

    public void cancel() {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        state = State.CANCELLED;
        terminationMessage = "Cancelled";
    }

    public boolean isRunning() {
        return state == State.INIT || state == State.LOADING || state == State.PASTING;
    }

    private void fail(String reason, Object... args) {
        state = State.FAILED;
        terminationMessage = String.format(reason, args);
    }

    public void notifyTermination() {
        if (sourceStack != null && !terminationMessage.isEmpty()) {
            sourceStack.sendFailure(Component.literal("Paste of " + location + " in " + dimensionId + " @ " + basePos + " terminated"));
            sourceStack.sendFailure(Component.literal(" - " + terminationMessage));
        }
    }

    public enum State {
        INIT(true),
        LOADING(true),
        PASTING(true),
        FAILED(false),
        FINISHED(false),
        CANCELLED(false);

        private final boolean running;

        State(boolean running) {
            this.running = running;
        }
    }
}
