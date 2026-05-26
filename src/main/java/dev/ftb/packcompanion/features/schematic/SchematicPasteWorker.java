package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.FallingBlock;
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
import java.util.function.Consumer;

public class SchematicPasteWorker {
    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private static final String DEFAULT_SHELL_BLOCK_ID = "minecraft:barrier";

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

    private int phaseMinX, phaseMaxX, phaseMinY, phaseMaxY, phaseMinZ, phaseMaxZ;
    private int phaseCurX, phaseCurY, phaseCurZ;
    private boolean phaseChunkActive;

    private boolean sealEnabled;
    private boolean cleanupFluidsEnabled;
    private boolean cleanupFallingEnabled;
    private boolean removeShell;
    private int cleanupScanMultiplier;
    private String shellBlockId;
    private BlockState shellState;
    private int shellFace;

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
            String resumePhase = c.contains("resumePhase") ? c.getString("resumePhase") : "PASTING";
            worker.state = State.INIT;
            worker.resumePhaseHint = resumePhase;
            worker.sealEnabled = c.getBoolean("sealEnabled");
            worker.cleanupFluidsEnabled = c.getBoolean("cleanupFluidsEnabled");
            worker.cleanupFallingEnabled = c.getBoolean("cleanupFallingEnabled");
            worker.removeShell = c.getBoolean("removeShell");
            worker.cleanupScanMultiplier = c.contains("cleanupScanMultiplier") ? c.getInt("cleanupScanMultiplier") : 20;
            worker.shellBlockId = c.contains("shellBlockId") ? c.getString("shellBlockId") : DEFAULT_SHELL_BLOCK_ID;
            worker.configSnapshotLoaded = true;
            return Optional.of(worker);
        }
        return Optional.empty();
    }

    private String resumePhaseHint = null;
    private boolean configSnapshotLoaded = false;

    public Tag saveNBT() {
        return Util.make(new CompoundTag(), tag -> {
            tag.putString("schematic", location.toString());
            tag.putString("dimensionId", dimensionId.toString());
            tag.put("basePos", NbtUtils.writeBlockPos(basePos));
            tag.putInt("speed", speed);
            tag.putInt("completedChunks", completedChunks);
            if (perTick) tag.putBoolean("perTick", true);
            tag.putString("resumePhase", currentPhaseTag());
            tag.putBoolean("sealEnabled", sealEnabled);
            tag.putBoolean("cleanupFluidsEnabled", cleanupFluidsEnabled);
            tag.putBoolean("cleanupFallingEnabled", cleanupFallingEnabled);
            tag.putBoolean("removeShell", removeShell);
            tag.putInt("cleanupScanMultiplier", cleanupScanMultiplier);
            if (shellBlockId != null) tag.putString("shellBlockId", shellBlockId);
        });
    }

    private String currentPhaseTag() {
        return switch (state) {
            case SEAL_PRELOAD, SEALING -> "SEALING";
            case CLEAN_PRELOAD, CLEANING -> "CLEANING";
            default -> "PASTING";
        };
    }

    public void tick(MinecraftServer server, int globalLimit) {
        switch (state) {
            case INIT -> {
                if (level == null) {
                    level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
                    if (level == null) {
                        SchematicPasteManager.LOGGER.error("unknown level {}", dimensionId);
                        fail("Invalid level %s", dimensionId);
                        return;
                    }
                }
                if (!configSnapshotLoaded) {
                    sealEnabled = PCServerConfig.SCHEMATIC_SEAL_PERIMETER.get();
                    cleanupFluidsEnabled = PCServerConfig.SCHEMATIC_CLEANUP_FLUIDS.get();
                    cleanupFallingEnabled = PCServerConfig.SCHEMATIC_CLEANUP_FALLING_BLOCKS.get();
                    removeShell = PCServerConfig.SCHEMATIC_REMOVE_SHELL_AFTER_PASTE.get();
                    cleanupScanMultiplier = PCServerConfig.SCHEMATIC_CLEANUP_SCAN_MULTIPLIER.get();
                    shellBlockId = PCServerConfig.SCHEMATIC_SHELL_BLOCK.get();
                }
                shellState = resolveShellState(shellBlockId);
                state = State.LOADING;
                loadSchematicDataAsync(server);
            }
            case SEAL_PRELOAD -> tickPreload(State.SEALING, this::buildShellChunkQueue, this::beginShellChunk);
            case SEALING -> tickShell(globalLimit);
            case PRELOAD -> tickPreload(State.PASTING, this::buildPasteChunkQueue, this::beginPastingChunk);
            case PASTING -> tickPaste(globalLimit);
            case CLEAN_PRELOAD -> tickPreload(State.CLEANING, this::buildCleanupChunkQueue, this::beginCleanupChunk);
            case CLEANING -> tickCleanup(globalLimit);
            default -> {}
        }
    }

    private void tickPreload(State workState, Runnable queueBuilder, Consumer<ChunkPos> chunkBeginner) {
        if (chunkQueue == null) {
            queueBuilder.run();
            for (int i = 0; i < completedChunks && !chunkQueue.isEmpty(); i++) {
                chunkQueue.pollFirst();
            }
            SchematicPasteManager.LOGGER.info("schematic {} covers {} chunks ({} remaining)", currentPhaseTag(), totalChunks, chunkQueue.size());
        }
        if (chunkQueue.isEmpty()) {
            advancePhase();
            return;
        }
        ChunkPos cp = chunkQueue.peek();
        if (level.hasChunk(cp.x, cp.z)) {
            chunkBeginner.accept(cp);
            phaseChunkActive = true;
            state = workState;
        } else if (forcedChunks.add(cp)) {
            level.setChunkForced(cp.x, cp.z, true);
        }
    }

    private void advancePhase() {
        chunkQueue = null;
        completedChunks = 0;
        totalChunks = 0;
        phaseChunkActive = false;
        switch (state) {
            case SEAL_PRELOAD, SEALING -> state = State.PRELOAD;
            case PRELOAD, PASTING -> state = cleanupEnabled() ? State.CLEAN_PRELOAD : State.FINISHED;
            case CLEAN_PRELOAD, CLEANING -> state = State.FINISHED;
            default -> state = State.FINISHED;
        }
    }

    private boolean cleanupEnabled() {
        return cleanupFluidsEnabled || cleanupFallingEnabled || (sealEnabled && removeShell);
    }

    private void buildPasteChunkQueue() {
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
    }

    private void tickPaste(int globalLimit) {
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
            if (!advanceWithinPasteChunk()) {
                finishCurrentChunk(true);
                if (chunkQueue.isEmpty()) {
                    advancePhase();
                } else {
                    state = State.PRELOAD;
                }
                return;
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

    private boolean advanceWithinPasteChunk() {
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

    private void finishCurrentChunk(boolean applyBiomes) {
        ChunkPos cp = chunkQueue.pollFirst();
        if (cp != null) {
            if (applyBiomes) {
                applyBiomesForChunk(cp);
            }
            if (forcedChunks.remove(cp)) {
                level.setChunkForced(cp.x, cp.z, false);
            }
        }
        completedChunks++;
        phaseChunkActive = false;
    }

    private void buildShellChunkQueue() {
        chunkQueue = new ArrayDeque<>();
        int minCX = (basePos.getX() - 1) >> 4;
        int maxCX = (basePos.getX() + data.getWidth()) >> 4;
        int minCZ = (basePos.getZ() - 1) >> 4;
        int maxCZ = (basePos.getZ() + data.getLength()) >> 4;
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                chunkQueue.addLast(new ChunkPos(cx, cz));
            }
        }
        totalChunks = chunkQueue.size();
    }

    private void beginShellChunk(ChunkPos cp) {
        shellFace = 0;
        setupShellFace(cp);
    }

    private void tickShell(int globalLimit) {
        int placed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (placed < blocksPerTick && placed < globalLimit) {
            if (!phaseChunkActive) return;
            if (shellFace >= 6) {
                finishShellChunk();
                return;
            }
            cursor.set(phaseCurX, phaseCurY, phaseCurZ);
            level.setBlock(cursor, shellState, Block.UPDATE_CLIENTS);
            placed++;
            if (!advanceWithinPhaseChunk()) {
                shellFace++;
                ChunkPos cp = chunkQueue.peek();
                if (cp == null || !setupShellFace(cp)) {
                    finishShellChunk();
                    return;
                }
            }
        }
    }

    private void finishShellChunk() {
        finishCurrentChunk(false);
        if (chunkQueue.isEmpty()) {
            advancePhase();
        } else {
            state = State.SEAL_PRELOAD;
        }
    }

    private boolean setupShellFace(ChunkPos cp) {
        int boxMinX = basePos.getX() - 1;
        int boxMaxX = basePos.getX() + data.getWidth();
        int boxMinY = basePos.getY() - 1;
        int boxMaxY = basePos.getY() + data.getHeight();
        int boxMinZ = basePos.getZ() - 1;
        int boxMaxZ = basePos.getZ() + data.getLength();

        int cMinX = Math.max(cp.x * 16, boxMinX);
        int cMaxX = Math.min(cp.x * 16 + 15, boxMaxX);
        int cMinZ = Math.max(cp.z * 16, boxMinZ);
        int cMaxZ = Math.min(cp.z * 16 + 15, boxMaxZ);

        int worldMinY = level.getMinBuildHeight();
        int worldMaxY = level.getMaxBuildHeight() - 1;
        int wallMinY = Math.max(boxMinY + 1, worldMinY);
        int wallMaxY = Math.min(boxMaxY - 1, worldMaxY);

        while (shellFace < 6) {
            boolean ok = false;
            switch (shellFace) {
                case 0 -> {
                    if (boxMinY >= worldMinY && boxMinY <= worldMaxY) {
                        setPhaseBounds(cMinX, cMaxX, boxMinY, boxMinY, cMinZ, cMaxZ);
                        ok = true;
                    }
                }
                case 1 -> {
                    if (boxMaxY >= worldMinY && boxMaxY <= worldMaxY) {
                        setPhaseBounds(cMinX, cMaxX, boxMaxY, boxMaxY, cMinZ, cMaxZ);
                        ok = true;
                    }
                }
                case 2 -> {
                    if (boxMinZ >= cMinZ && boxMinZ <= cMaxZ && wallMinY <= wallMaxY) {
                        setPhaseBounds(cMinX, cMaxX, wallMinY, wallMaxY, boxMinZ, boxMinZ);
                        ok = true;
                    }
                }
                case 3 -> {
                    if (boxMaxZ >= cMinZ && boxMaxZ <= cMaxZ && wallMinY <= wallMaxY) {
                        setPhaseBounds(cMinX, cMaxX, wallMinY, wallMaxY, boxMaxZ, boxMaxZ);
                        ok = true;
                    }
                }
                case 4 -> {
                    if (boxMinX >= cMinX && boxMinX <= cMaxX && wallMinY <= wallMaxY) {
                        int innerMinZ = Math.max(boxMinZ + 1, cMinZ);
                        int innerMaxZ = Math.min(boxMaxZ - 1, cMaxZ);
                        if (innerMinZ <= innerMaxZ) {
                            setPhaseBounds(boxMinX, boxMinX, wallMinY, wallMaxY, innerMinZ, innerMaxZ);
                            ok = true;
                        }
                    }
                }
                case 5 -> {
                    if (boxMaxX >= cMinX && boxMaxX <= cMaxX && wallMinY <= wallMaxY) {
                        int innerMinZ = Math.max(boxMinZ + 1, cMinZ);
                        int innerMaxZ = Math.min(boxMaxZ - 1, cMaxZ);
                        if (innerMinZ <= innerMaxZ) {
                            setPhaseBounds(boxMaxX, boxMaxX, wallMinY, wallMaxY, innerMinZ, innerMaxZ);
                            ok = true;
                        }
                    }
                }
            }
            if (ok) return true;
            shellFace++;
        }
        return false;
    }

    private void setPhaseBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        phaseMinX = minX; phaseMaxX = maxX;
        phaseMinY = minY; phaseMaxY = maxY;
        phaseMinZ = minZ; phaseMaxZ = maxZ;
        phaseCurX = minX; phaseCurY = minY; phaseCurZ = minZ;
    }

    private BlockState resolveShellState(String id) {
        ResourceLocation loc = id != null ? ResourceLocation.tryParse(id) : null;
        if (loc != null) {
            Block block = BuiltInRegistries.BLOCK.getOptional(loc).orElse(null);
            if (block != null && block != Blocks.AIR) {
                return block.defaultBlockState();
            }
        }
        SchematicPasteManager.LOGGER.warn("invalid schematic shell_block '{}', falling back to {}", id, DEFAULT_SHELL_BLOCK_ID);
        return Blocks.BARRIER.defaultBlockState();
    }

    private boolean isOnShellLayer(int wx, int wy, int wz) {
        boolean insideX = wx >= basePos.getX() && wx < basePos.getX() + data.getWidth();
        boolean insideY = wy >= basePos.getY() && wy < basePos.getY() + data.getHeight();
        boolean insideZ = wz >= basePos.getZ() && wz < basePos.getZ() + data.getLength();
        return !(insideX && insideY && insideZ);
    }

    private void buildCleanupChunkQueue() {
        if (sealEnabled && removeShell) {
            buildShellChunkQueue();
        } else {
            buildPasteChunkQueue();
        }
    }

    private void beginCleanupChunk(ChunkPos cp) {
        if (sealEnabled && removeShell) {
            beginPhaseChunk(cp, basePos.getX() - 1, basePos.getX() + data.getWidth(),
                    basePos.getY() - 1, basePos.getY() + data.getHeight(),
                    basePos.getZ() - 1, basePos.getZ() + data.getLength());
        } else {
            beginPhaseChunk(cp, basePos.getX(), basePos.getX() + data.getWidth() - 1,
                    basePos.getY(), basePos.getY() + data.getHeight() - 1,
                    basePos.getZ(), basePos.getZ() + data.getLength() - 1);
        }
    }

    private void tickCleanup(int globalLimit) {
        int writeBudget = blocksPerTick;
        int scanBudget = blocksPerTick * cleanupScanMultiplier;
        int globalScanBudget = (globalLimit == Integer.MAX_VALUE) ? Integer.MAX_VALUE : globalLimit * cleanupScanMultiplier;
        int scanned = 0;
        int written = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (scanned < scanBudget && scanned < globalScanBudget && written < writeBudget) {
            if (!phaseChunkActive) return;
            cursor.set(phaseCurX, phaseCurY, phaseCurZ);
            if (shouldCleanCell(phaseCurX, phaseCurY, phaseCurZ)) {
                BlockState worldState = level.getBlockState(cursor);
                if (!worldState.isAir() && isCleanupTarget(phaseCurX, phaseCurY, phaseCurZ, worldState)) {
                    level.setBlock(cursor, AIR_STATE, Block.UPDATE_CLIENTS);
                    written++;
                }
            }
            scanned++;
            if (!advanceWithinPhaseChunk()) {
                finishCurrentChunk(false);
                if (chunkQueue.isEmpty()) {
                    advancePhase();
                } else {
                    state = State.CLEAN_PRELOAD;
                }
                return;
            }
        }
    }

    private boolean shouldCleanCell(int wx, int wy, int wz) {
        boolean inside = !isOnShellLayer(wx, wy, wz);
        if (inside) {
            int sx = wx - basePos.getX();
            int sy = wy - basePos.getY();
            int sz = wz - basePos.getZ();
            BlockState schemState = data.getBlockAt(currentPos.set(sx, sy, sz), AIR_STATE);
            return schemState.isAir();
        }
        return sealEnabled && removeShell;
    }

    private boolean isCleanupTarget(int wx, int wy, int wz, BlockState worldState) {
        boolean onShell = isOnShellLayer(wx, wy, wz);
        if (onShell) {
            return worldState.is(shellState.getBlock());
        }
        if (sealEnabled && removeShell && worldState.is(shellState.getBlock())) {
            return true;
        }
        if (cleanupFluidsEnabled && (worldState.is(Blocks.WATER) || worldState.is(Blocks.LAVA))) {
            return true;
        }
        if (cleanupFallingEnabled && worldState.getBlock() instanceof FallingBlock) {
            return true;
        }
        return false;
    }

    private void beginPhaseChunk(ChunkPos cp, int wMinX, int wMaxX, int wMinY, int wMaxY, int wMinZ, int wMaxZ) {
        phaseMinX = Math.max(cp.x * 16, wMinX);
        phaseMaxX = Math.min(cp.x * 16 + 15, wMaxX);
        phaseMinZ = Math.max(cp.z * 16, wMinZ);
        phaseMaxZ = Math.min(cp.z * 16 + 15, wMaxZ);
        phaseMinY = Math.max(wMinY, level.getMinBuildHeight());
        phaseMaxY = Math.min(wMaxY, level.getMaxBuildHeight() - 1);
        phaseCurX = phaseMinX;
        phaseCurY = phaseMinY;
        phaseCurZ = phaseMinZ;
    }

    private boolean advanceWithinPhaseChunk() {
        phaseCurX++;
        if (phaseCurX > phaseMaxX) {
            phaseCurX = phaseMinX;
            phaseCurZ++;
            if (phaseCurZ > phaseMaxZ) {
                phaseCurZ = phaseMinZ;
                phaseCurY++;
                if (phaseCurY > phaseMaxY) {
                    return false;
                }
            }
        }
        return true;
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
                state = nextStateAfterLoading();
            } catch (IOException e) {
                SchematicPasteManager.LOGGER.error("can't open resource {}: {}", location, e.getMessage());
                fail("Schematic %s can't be read: %s", location, e.getMessage());
            }
        }, () -> {
            SchematicPasteManager.LOGGER.error("unknown resource {}", location);
            fail("Resource %s does not exist in resource manager", location);
        }));
    }

    private State nextStateAfterLoading() {
        String hint = resumePhaseHint;
        resumePhaseHint = null;
        if ("CLEANING".equals(hint)) {
            return cleanupEnabled() ? State.CLEAN_PRELOAD : State.FINISHED;
        }
        if ("PASTING".equals(hint)) {
            return State.PRELOAD;
        }
        return sealEnabled ? State.SEAL_PRELOAD : State.PRELOAD;
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
        SEAL_PRELOAD(true),
        SEALING(true),
        PRELOAD(true),
        PASTING(true),
        CLEAN_PRELOAD(true),
        CLEANING(true),
        FAILED(false),
        FINISHED(false),
        CANCELLED(false);

        private final boolean running;

        State(boolean running) {
            this.running = running;
        }
    }
}
