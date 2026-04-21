package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SchematicPasteWorker {
    public static final Codec<SchematicPasteWorker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("schematic").forGetter(w -> w.location),
            Identifier.CODEC.fieldOf("dimensionId").forGetter(w -> w.dimensionId),
            BlockPos.CODEC.fieldOf("basePos").forGetter(w -> w.basePos),
            Codec.INT.fieldOf("speed").forGetter(w -> w.blocksPerTick),
            BlockPos.CODEC.fieldOf("currentPos").forGetter(w -> w.currentPos)
    ).apply(instance, SchematicPasteWorker::new));

    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private final Identifier location;
    private final BlockPos basePos;
    private final int blocksPerTick;  // blocks per tick
    private final BlockPos.MutableBlockPos currentPos;
    @Nullable
    private final CommandSourceStack sourceStack;
    private Identifier dimensionId;
    private ServerLevel level;
    private SchematicData data;
    private State state;
    private CompletableFuture<Void> future;
    private String terminationMessage = "";

    public SchematicPasteWorker(@Nullable CommandSourceStack sourceStack, Identifier location, Either<ServerLevel,Identifier> levelOrDimensionId, BlockPos basePos, int blocksPerTick, BlockPos.MutableBlockPos currentPos) {
        this.sourceStack = sourceStack;
        this.location = location;
        this.basePos = basePos;
        this.blocksPerTick = blocksPerTick;
        levelOrDimensionId
                .ifLeft(level -> {
                    this.level = level;
                    this.dimensionId = level.dimension().identifier();
                })
                .ifRight(dimId -> this.dimensionId = dimId);
        state = State.INIT;
        this.currentPos = currentPos;
    }

    public SchematicPasteWorker(@Nullable CommandSourceStack sourceStack, Identifier location, Either<ServerLevel,Identifier> levelOrDimensionId, BlockPos basePos, int blocksPerTick) {
        this(sourceStack, location, levelOrDimensionId, basePos, blocksPerTick, BlockPos.ZERO.mutable());
    }

    public SchematicPasteWorker(Identifier location, Identifier dimensionId, BlockPos basePos, int blocksPerTick, BlockPos currentPos) {
        this(null, location, Either.right(dimensionId), basePos, blocksPerTick, currentPos.mutable());
    }

//    public static Optional<SchematicPasteWorker> fromNBT(Tag tag) {
//        if (tag instanceof CompoundTag c) {
//            SchematicPasteWorker worker = new SchematicPasteWorker(
//                    null,
//                    Identifier.tryParse(c.getString("schematic")),
//                    Either.right(Identifier.tryParse(c.getString("dimensionId"))),
//                    NbtUtils.readBlockPos(c, "basePos").orElse(BlockPos.ZERO),
//                    c.getInt("speed")
//            );
//            worker.currentPos.set(NbtUtils.readBlockPos(c, "currentPos").orElse(BlockPos.ZERO).mutable());
//            return Optional.of(worker);
//        }
//        return Optional.empty();
//    }
//
//    public Tag saveNBT() {
//        return Util.make(new CompoundTag(), tag -> {
//            tag.putString("schematic", location.toString());
//            tag.putString("dimensionId", dimensionId.toString());
//            tag.put("basePos", NbtUtils.writeBlockPos(basePos));
//            tag.putInt("speed", blocksPerTick);
//            tag.put("currentPos", NbtUtils.writeBlockPos(currentPos));
//        });
//    }

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
                        ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), beData);
                        be.loadWithComponents(valueInput);
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
                if (currentPos.getY() >= data.getHeight() || currentPos.getY() >= level.getMaxY()) {
                    state = State.FINISHED;
                }
            }
        }
    }

    private void loadSchematicDataAsync(MinecraftServer server) {
        var fullLoc = location.withPath(p -> "schematics/" + p + ".schem");
        future = CompletableFuture.runAsync(() -> server.getResourceManager().getResource(fullLoc).ifPresentOrElse(resource -> {
            try (var in = resource.open()) {
                CompoundTag schemTag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
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

    public Identifier getDimensionId() {
        return dimensionId;
    }

    public Identifier makeKey() {
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
