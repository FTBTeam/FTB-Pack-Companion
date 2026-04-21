package dev.ftb.packcompanion.features.schematic;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SchematicPasteManager extends SavedData {
    private static final Codec<SchematicPasteManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SchematicPasteWorker.CODEC.listOf().fieldOf("workers")
                    .forGetter(data -> data.workers.values().stream()
                            .flatMap(map -> map.values().stream())
                            .toList())
    ).apply(instance, SchematicPasteManager::new));

    private static final SavedDataType<SchematicPasteManager> TYPE = new SavedDataType<>(
            PackCompanion.id("schematic_paste_manager"),
            SchematicPasteManager::new,
            CODEC
    );

    static final Logger LOGGER = LoggerFactory.getLogger(SchematicPasteManager.class);

    private final Map<Identifier, Map<Identifier, SchematicPasteWorker>> workers = new ConcurrentHashMap<>();

    private SchematicPasteManager() {
    }

    private SchematicPasteManager(Iterable<SchematicPasteWorker> workers) {
        workers.forEach(this::addWorker);
    }

    public static SchematicPasteManager getInstance(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

//    private static SchematicPasteManager load(CompoundTag compoundTag, HolderLookup.Provider provider) {
//        return new SchematicPasteManager().readNBT(compoundTag);
//    }

//    private SchematicPasteManager readNBT(CompoundTag compoundTag) {
//        workers.clear();
//        compoundTag.getList("workers", Tag.TAG_COMPOUND)
//                .forEach(tag -> SchematicPasteWorker.fromNBT(tag).ifPresent(this::addWorker));
//        return this;
//    }
//
//    @Override
//    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
//        ListTag list = new ListTag();
//        workers.forEach((dimId, map) -> map.values().forEach((v -> list.add(v.saveNBT()))));
//        compoundTag.put("workers", list);
//        return compoundTag;
//    }

    public void tick(MinecraftServer server) {
        workers.forEach((dimId, map) -> {
            Set<Identifier> toRemove = new HashSet<>();
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

    public void startPaste(CommandSourceStack sourceStack, Identifier location, BlockPos basePos, int blocksPerTick) {
        addWorker(new SchematicPasteWorker(sourceStack, location, Either.left(sourceStack.getLevel()), basePos, blocksPerTick));
    }

    private void addWorker(SchematicPasteWorker worker) {
        workers.computeIfAbsent(worker.getDimensionId(), k -> new ConcurrentHashMap<>())
                .put(worker.makeKey(), worker);
        setDirty();
    }

    public Stream<Pair<Identifier,SchematicPasteWorker>> list() {
        return workers.values().stream()
                .flatMap(m -> m.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                );
    }

    public boolean cancelPaste(Identifier dimId, Identifier workerId) {
        SchematicPasteWorker worker = workers.getOrDefault(dimId, Map.of()).get(workerId);
        if (worker != null && worker.isRunning()) {
            worker.cancel();
            return true;
        }
        return false;
    }
}
