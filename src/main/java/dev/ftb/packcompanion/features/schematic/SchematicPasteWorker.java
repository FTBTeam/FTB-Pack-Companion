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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SchematicPasteWorker {
    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private final ResourceLocation location;
    private final BlockPos basePos;
    private final int speed;  // blocks per tick, or total paste time
    private final boolean perTick;
    private final BlockPos.MutableBlockPos currentPos;
    @Nullable
    private final CommandSourceStack sourceStack;
    private ResourceLocation dimensionId;
    private ServerLevel level;
    private SchematicData data;
    private volatile State state;
    private CompletableFuture<Void> future;
    private String terminationMessage = "";
    private int blocksPerTick;
    private Deque<ChunkPos> preloadNeeded;
    private final Set<ChunkPos> forcedChunks = new HashSet<>();

    public SchematicPasteWorker(@Nullable CommandSourceStack sourceStack, ResourceLocation location, Either<ServerLevel,ResourceLocation> levelOrDimensionId, BlockPos basePos, int speed, boolean perTick) {
        this.sourceStack = sourceStack;
        this.location = location;
        this.basePos = basePos;
        this.speed = speed;
        this.perTick = perTick;
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
                    c.getInt("speed"),
                    c.getBoolean("perTick"));
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
            tag.putInt("speed", speed);
            tag.put("currentPos", NbtUtils.writeBlockPos(currentPos));
            if (perTick) tag.putBoolean("perTick", true);
        });
    }

    public void tick(MinecraftServer server, int globalLimit) {
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
        } else if (state == State.PRELOAD) {
            if (preloadNeeded == null) {
                // first tick of pregen: determine which affected chunks are not currently loaded
                preloadNeeded = new ArrayDeque<>();
                for (int x = basePos.getX(); x <= basePos.getX() + data.getWidth(); x += 16) {
                    for (int z = basePos.getZ(); z <= basePos.getZ() + data.getLength(); z += 16) {
                        ChunkPos cp = new ChunkPos(x >> 4, z >> 4);
                        if (!level.hasChunk(cp.x, cp.z)) {
                            preloadNeeded.addLast(cp);
                        }
                    }
                }
                SchematicPasteManager.LOGGER.info("need to load {} chunks", preloadNeeded.size());
            } else if (preloadNeeded.isEmpty()) {
                // done preloading!
                state = State.PASTING;
            } else {
                // non-blocking: force-load the chunk and poll until it's ready
                ChunkPos cp = preloadNeeded.peek();
                if (level.hasChunk(cp.x, cp.z)) {
                    preloadNeeded.pop();
                } else if (forcedChunks.add(cp)) {
                    level.setChunkForced(cp.x, cp.z, true);
                }
                // chunk will be loaded asynchronously by the chunk system; check again next tick
            }
        } else if (state == State.PASTING) {
            int pasted = 0;
            while (pasted < blocksPerTick && pasted < globalLimit) {
                BlockPos destPos = basePos.offset(currentPos);
                // note: only count a block as pasted (for limit purposes) if it actually changed the level's blockstate
                if (level.setBlock(destPos, data.getBlockAt(currentPos, AIR_STATE), Block.UPDATE_CLIENTS)) {
                    data.getBlockEntityDataAt(currentPos).ifPresent(beData -> {
                        try {
                            BlockEntity be = level.getBlockEntity(destPos);
                            if (be != null) {
                                be.load(beData);
                            }
                        } catch (Exception e) {
                            SchematicPasteFeature.LOGGER.error("caught exception while loading block entity data at {}: {} / {}",
                                    destPos, e.getClass().getName(), e.getMessage());
                        }
                    });
                    pasted++;
                }
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
                    releaseForcedChunks();
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
                blocksPerTick = perTick ? speed : data.getTotalBlockCount() / speed;
                state = State.PRELOAD; // volatile write — must be AFTER data & blocksPerTick are set
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

    public int getProgress() {
        if (data == null) return 0;
        int c = currentPos.getX()
                + currentPos.getZ() * data.getWidth()
                + currentPos.getY() * data.getWidth() * data.getHeight();

        return c * 100 / data.getTotalBlockCount();
    }

    public boolean isRunning() {
        return state.running;
    }

    public void cancel() {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        state = State.CANCELLED;
        terminationMessage = "Cancelled";
        releaseForcedChunks();
    }

    private void fail(String reason, Object... args) {
        state = State.FAILED;
        terminationMessage = String.format(reason, args);
        releaseForcedChunks();
    }

    private void releaseForcedChunks() {
        if (level != null) {
            for (ChunkPos cp : forcedChunks) {
                level.setChunkForced(cp.x, cp.z, false);
            }
        }
        forcedChunks.clear();
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
        PRELOAD(true),
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
