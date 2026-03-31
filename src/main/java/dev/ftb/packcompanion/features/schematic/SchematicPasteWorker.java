package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SchematicPasteWorker {
    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private final ResourceLocation location;
    private final BlockPos basePos;
    private final int speed;
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
    private Deque<ChunkPos> chunkQueue;
    private int completedChunks;
    private int totalChunks;
    private int chunkMinSX, chunkMaxSX, chunkMinSZ, chunkMaxSZ;
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
            worker.completedChunks = c.getInt("completedChunks");
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
            tag.putInt("completedChunks", completedChunks);
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
            if (chunkQueue == null) {
                chunkQueue = new ArrayDeque<>();
                int minCX = basePos.getX() >> 4;
                int maxCX = (basePos.getX() + data.getWidth() - 1) >> 4;
                int minCZ = basePos.getZ() >> 4;
                int maxCZ = (basePos.getZ() + data.getLength() - 1) >> 4;
                for (int cx = minCX; cx <= maxCX; cx++) {
                    for (int cz = minCZ; cz <= maxCZ; cz++) {
                        chunkQueue.addLast(new ChunkPos(cx, cz));
                    }
                }
                totalChunks = chunkQueue.size();
                for (int i = 0; i < completedChunks && !chunkQueue.isEmpty(); i++) {
                    chunkQueue.pollFirst();
                }
                SchematicPasteManager.LOGGER.info("schematic covers {} chunks, {} remaining", totalChunks, chunkQueue.size());
            }
            if (chunkQueue.isEmpty()) {
                state = State.FINISHED;
                return;
            }
            ChunkPos cp = chunkQueue.peek();
            if (level.hasChunk(cp.x, cp.z)) {
                beginPastingChunk(cp);
                state = State.PASTING;
            } else if (forcedChunks.add(cp)) {
                level.setChunkForced(cp.x, cp.z, true);
            }
        } else if (state == State.PASTING) {
            int pasted = 0;
            while (pasted < blocksPerTick && pasted < globalLimit) {
                BlockPos destPos = basePos.offset(currentPos);
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
                if (!advanceWithinChunk()) {
                    finishCurrentChunk();
                    if (chunkQueue.isEmpty()) {
                        state = State.FINISHED;
                    } else {
                        state = State.PRELOAD;
                    }
                    return;
                }
            }
        }
    }

    private void beginPastingChunk(ChunkPos cp) {
        chunkMinSX = Math.max(0, cp.x * 16 - basePos.getX());
        chunkMaxSX = Math.min(data.getWidth() - 1, cp.x * 16 + 15 - basePos.getX());
        chunkMinSZ = Math.max(0, cp.z * 16 - basePos.getZ());
        chunkMaxSZ = Math.min(data.getLength() - 1, cp.z * 16 + 15 - basePos.getZ());
        currentPos.set(chunkMinSX, 0, chunkMinSZ);
    }

    private boolean advanceWithinChunk() {
        int x = currentPos.getX() + 1;
        if (x > chunkMaxSX) {
            x = chunkMinSX;
            int z = currentPos.getZ() + 1;
            if (z > chunkMaxSZ) {
                int y = currentPos.getY() + 1;
                if (y >= data.getHeight() || basePos.getY() + y >= level.getMaxBuildHeight()) {
                    return false;
                }
                currentPos.set(x, y, chunkMinSZ);
                return true;
            }
            currentPos.set(x, currentPos.getY(), z);
            return true;
        }
        currentPos.set(x, currentPos.getY(), currentPos.getZ());
        return true;
    }

    private void finishCurrentChunk() {
        ChunkPos cp = chunkQueue.pollFirst();
        if (cp != null) {
            applyBiomesForChunk(cp);
            if (forcedChunks.remove(cp)) {
                level.setChunkForced(cp.x, cp.z, false);
            }
        }
        completedChunks++;
    }

    @SuppressWarnings("unchecked")
    private void applyBiomesForChunk(ChunkPos cp) {
        if (!data.hasBiomeData()) return;

        LevelChunk chunk = level.getChunk(cp.x, cp.z);
        boolean modified = false;

        int minWX = Math.max(basePos.getX(), cp.getMinBlockX());
        int maxWX = Math.min(basePos.getX() + data.getWidth() - 1, cp.getMaxBlockX());
        int minWZ = Math.max(basePos.getZ(), cp.getMinBlockZ());
        int maxWZ = Math.min(basePos.getZ() + data.getLength() - 1, cp.getMaxBlockZ());
        int minWY = basePos.getY();
        int maxWY = Math.min(basePos.getY() + data.getHeight() - 1, level.getMaxBuildHeight() - 1);

        int minBX = minWX >> 2, maxBX = maxWX >> 2;
        int minBY = minWY >> 2, maxBY = maxWY >> 2;
        int minBZ = minWZ >> 2, maxBZ = maxWZ >> 2;

        for (int wby = minBY; wby <= maxBY; wby++) {
            int wy = wby << 2;
            int sectionIndex = chunk.getSectionIndex(wy);
            if (sectionIndex < 0 || sectionIndex >= chunk.getSectionsCount()) continue;
            LevelChunkSection section = chunk.getSection(sectionIndex);
            PalettedContainer<Holder<Biome>> biomeContainer =
                    (PalettedContainer<Holder<Biome>>) (Object) section.getBiomes();

            for (int wbz = minBZ; wbz <= maxBZ; wbz++) {
                for (int wbx = minBX; wbx <= maxBX; wbx++) {
                    int sx = Math.max(0, Math.min((wbx << 2) - basePos.getX(), data.getWidth() - 1));
                    int sy = Math.max(0, Math.min((wby << 2) - basePos.getY(), data.getHeight() - 1));
                    int sz = Math.max(0, Math.min((wbz << 2) - basePos.getZ(), data.getLength() - 1));

                    Holder<Biome> biome = data.getBiomeAtCell(sx >> 2, sy >> 2, sz >> 2);
                    if (biome != null) {
                        biomeContainer.getAndSet(wbx & 3, (wy & 15) >> 2, wbz & 3, biome);
                        modified = true;
                    }
                }
            }
        }

        if (modified) {
            chunk.setUnsaved(true);
            level.getChunkSource().chunkMap.resendBiomesForChunks(List.<ChunkAccess>of(chunk));
        }
    }

    private void loadSchematicDataAsync(MinecraftServer server) {
        var fullLoc = location.withPath(p -> "schematics/" + p + ".schem");
        future = CompletableFuture.runAsync(() -> server.getResourceManager().getResource(fullLoc).ifPresentOrElse(resource -> {
            try (var in = resource.open()) {
                CompoundTag schemTag = NbtIo.readCompressed(in);
                data = SchematicData.load(server.registryAccess(), schemTag);
                blocksPerTick = perTick ? speed : data.getTotalBlockCount() / speed;
                state = State.PRELOAD;
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
        if (totalChunks == 0) return data != null ? 100 : 0;
        return completedChunks * 100 / totalChunks;
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
    }

    private void fail(String reason, Object... args) {
        state = State.FAILED;
        terminationMessage = String.format(reason, args);
    }

    private void releaseForcedChunks() {
        if (level != null) {
            for (ChunkPos cp : forcedChunks) {
                level.setChunkForced(cp.x, cp.z, false);
            }
        }
        forcedChunks.clear();
    }

    public void cleanup() {
        releaseForcedChunks();

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
