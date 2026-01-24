package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SchematicPasteManager extends SavedData {
    private static final String DATA_NAME = "ftb_schematic_paste";
    static final Logger LOGGER = LoggerFactory.getLogger(SchematicPasteManager.class);

    private final Map<ResourceLocation,Map<ResourceLocation, SchematicPasteWorker>> workers = new ConcurrentHashMap<>();

    private SchematicPasteManager() {
    }

    public static SchematicPasteManager getInstance(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(SchematicPasteManager::new, SchematicPasteManager::load), DATA_NAME);
    }

    private static SchematicPasteManager load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return new SchematicPasteManager().readNBT(compoundTag);
    }

    private SchematicPasteManager readNBT(CompoundTag compoundTag) {
        workers.clear();
        compoundTag.getList("workers", Tag.TAG_COMPOUND)
                .forEach(tag -> SchematicPasteWorker.fromNBT(tag).ifPresent(this::addWorker));
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        workers.forEach((dimId, map) -> map.values().forEach((v -> list.add(v.saveNBT()))));
        compoundTag.put("workers", list);
        return compoundTag;
    }

    public void tick(MinecraftServer server) {
        workers.forEach((dimId, map) -> {
            Set<ResourceLocation> toRemove = new HashSet<>();
            map.forEach((key, worker) -> {
                worker.tick(server);
                if (worker.isDone()) {
                    toRemove.add(key);
                    worker.notifyTermination();
                }
                setDirty();
            });
            toRemove.forEach(map::remove);
        });
    }

    public void startPaste(CommandSourceStack sourceStack, ResourceLocation location, BlockPos basePos, int blocksPerTick) {
        addWorker(new SchematicPasteWorker(sourceStack, location, Either.left(sourceStack.getLevel()), basePos, blocksPerTick));
    }

    private void addWorker(SchematicPasteWorker worker) {
        workers.computeIfAbsent(worker.getDimensionId(), k -> new ConcurrentHashMap<>())
                .put(worker.makeKey(), worker);
        setDirty();
    }

    public Stream<Pair<ResourceLocation,SchematicPasteWorker>> list() {
        return workers.values().stream()
                .flatMap(m -> m.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                );
    }

    public boolean cancelPaste(ResourceLocation dimId, ResourceLocation workerId) {
        SchematicPasteWorker worker = workers.getOrDefault(dimId, Map.of()).get(workerId);
        if (worker != null && worker.isRunning()) {
            worker.cancel();
            return true;
        }
        return false;
    }
}
