package dev.ftb.packcompanion.features.spawners;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SpawnerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerManager.class);

    private boolean initialized = false;
    private static final SpawnerManager INSTANCE = new SpawnerManager();
    private DataStore dataStore;

    // Defer loading so the config and registry are initialized
    private final LazyValue<List<EntityType<?>>> randomEntities = new LazyValue<>(() -> {
        List<String> randomEntities = PCServerConfig.SPAWNERS_USE_RANDOM_ENTITY.get();
        List<EntityType<?>> entities = new ArrayList<>();
        for (String entity : randomEntities) {
            EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(entity));
            if (entityType == EntityType.PIG && !entity.endsWith("pig")) {
                continue; // Failed as the registry default is pig
            }

            entities.add(entityType);
        }
        return entities;
    });

    private SpawnerManager() {

    }

    public void init(MinecraftServer server) {
        if (initialized) {
            return;
        }

        initialized = true;
        dataStore = DataStore.create(server);

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level == null || level.getServer() == null || level.isClientSide) {
                return EventResult.pass();
            }

            if (state.getBlock() != Blocks.SPAWNER) {
                return EventResult.pass();
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)) {
                return EventResult.pass();
            }

            // Spawn data
            var compound = spawnerBlockEntity.saveWithoutMetadata();
            DataStore dataStore = this.getDataStore();
            dataStore.brokenSpawners.add(new MobSpawnerData(pos, compound, level.dimension()));
            dataStore.setDirty();

            return EventResult.pass();
        });

        TickEvent.ServerLevelTick.SERVER_LEVEL_POST.register(this::onServerTick);
    }

    private void onServerTick(ServerLevel serverLevel) {
        // Only run this method every 10 seconds
        if (serverLevel.getGameTime() % 200 != 0) {
            return;
        }

        DataStore dataStore = this.getDataStore();
        if (dataStore.brokenSpawners.isEmpty()) {
            return;
        }

        List<MobSpawnerData> dimensionSpawners = dataStore.brokenSpawners.stream()
                .filter(e -> e.dimension().equals(serverLevel.dimension()))
                .toList();

        if (dimensionSpawners.isEmpty()) {
            return;
        }

        Instant currentTime = Instant.now();
        int respawnInterval = PCServerConfig.SPAWNERS_RESPAWN_INTERVAL.get();

        // Copy list to avoid concurrent modification
        for (MobSpawnerData spawnerData : dimensionSpawners) {
            if (currentTime.isBefore(spawnerData.breakTime.plus(respawnInterval, ChronoUnit.MINUTES))) {
                continue;
            }

            BlockState state = serverLevel.getBlockState(spawnerData.pos);
            if (!state.isAir() || !state.getMaterial().isReplaceable()) {
                // Can't respawn spawner
                dataStore.brokenSpawners.remove(spawnerData);
                dataStore.setDirty();
                continue;
            }

            // Respawn spawner
            serverLevel.setBlock(spawnerData.pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
            BlockEntity blockEntity = serverLevel.getBlockEntity(spawnerData.pos);
            if (!(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)) {
                continue;
            }

            // Set spawn data
            CompoundTag compound = spawnerData.spawnerData;
            List<EntityType<?>> randomEntities = this.randomEntities.get();
            // Only happens if the random entity list is not empty
            if (!randomEntities.isEmpty()) {
                EntityType<?> foundEntity;
                if (randomEntities.size() == 1) {
                    foundEntity = randomEntities.get(0);
                } else {
                    foundEntity = randomEntities.get(serverLevel.random.nextInt(randomEntities.size()));
                }

                var entityCompound = Util.make(new CompoundTag(), tag -> tag.put("entity",
                        Util.make(new CompoundTag(), entityTag -> entityTag.putString("id", Objects.requireNonNull(foundEntity.arch$registryName()).toString()))));

                compound.put("SpawnData", entityCompound);
            }

            spawnerBlockEntity.load(compound);
            spawnerBlockEntity.setChanged();
            dataStore.brokenSpawners.remove(spawnerData);
            dataStore.setDirty();
        }
    }

    public static SpawnerManager get() {
        return INSTANCE;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public record MobSpawnerData(
       BlockPos pos,
       CompoundTag spawnerData,
       Instant breakTime,
       ResourceKey<Level> dimension
    ) {
        private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registry.DIMENSION_REGISTRY);
        private static final Codec<Instant> INSTANT_CODEC = Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli);
        public static final Codec<MobSpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(MobSpawnerData::pos),
                CompoundTag.CODEC.fieldOf("spawner_data").forGetter(MobSpawnerData::spawnerData),
                INSTANT_CODEC.fieldOf("break_time").forGetter(MobSpawnerData::breakTime),
                DIMENSION_CODEC.fieldOf("dimension").forGetter(MobSpawnerData::dimension)
        ).apply(instance, MobSpawnerData::new));

        public MobSpawnerData(BlockPos pos, CompoundTag spawnerData, ResourceKey<Level> dimension) {
            this(pos, spawnerData, Instant.now(), dimension);
        }
    }

    public static class DataStore extends SavedData {
        private final List<MobSpawnerData> brokenSpawners = new ArrayList<>();

        private DataStore() {}

        private DataStore(CompoundTag tag) {
            if (!tag.contains("broken_spawners")) {
                return;
            }

            brokenSpawners.addAll(MobSpawnerData.CODEC.listOf().parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("broken_spawners"))).result().orElse(new ArrayList<>()));
        }

        public static DataStore create(MinecraftServer server) {
            return server.getLevel(Level.OVERWORLD).getDataStorage()
                    .computeIfAbsent(DataStore::new, DataStore::new, "ftbpc-spawner-manager");
        }

        @Override
        @NotNull
        public CompoundTag save(CompoundTag compoundTag) {
            compoundTag.put("broken_spawners", MobSpawnerData.CODEC.listOf().encodeStart(NbtOps.INSTANCE, brokenSpawners).result().orElse(new CompoundTag()));
            return compoundTag;
        }

        public List<MobSpawnerData> getBrokenSpawners() {
            return brokenSpawners;
        }
    }

    /**
     * A lazy value that is only computed once
     */
    public static class LazyValue<T> implements Supplier<T> {
        private volatile T value;
        private final Supplier<T> supplier;

        public LazyValue(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        value = supplier.get();
                    }
                }
            }
            return value;
        }
    }
}
