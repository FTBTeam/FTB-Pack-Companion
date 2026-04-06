package dev.ftb.packcompanion.features.spawners;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpawnerFeature extends Feature.Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerFeature.class);

    private DataStore dataStore;

    public SpawnerFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (!PCServerConfig.SPAWNERS_ALLOW_RESPAWN.get()) {
            return; // Module is disabled
        }

        modEventBus.addListener(this::onBlockBroken);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @Override
    public void onServerInit(MinecraftServer server) {
        dataStore = DataStore.create(server);
    }

    @Override
    public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        return List.of(Commands.literal("spawner_manager").then(Commands.literal("clear").executes(this::clearBrokenSpawners)));
    }

    // Defer loading so the config and registry are initialized
    private final Supplier<List<EntityType<?>>> randomEntities = Suppliers.memoize(() -> {
        List<String> randomEntities = PCServerConfig.SPAWNERS_USE_RANDOM_ENTITY.get();
        List<EntityType<?>> entities = new ArrayList<>();
        for (String entity : randomEntities) {
            Identifier resourceLocation = Identifier.tryParse(entity);
            if (resourceLocation == null) {
                continue;
            }

            Optional<Holder.Reference<EntityType<?>>> entityType = BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
            if (entityType.isEmpty()) {
                continue;
            }

            entities.add(entityType.get().value());
        }
        return entities;
    });

    public void onBlockBroken(BlockEvent.BreakEvent event) {
        var level = event.getLevel();
        var state = event.getState();
        var pos = event.getPos();
        var player = event.getPlayer();

        if (level.getServer() == null || level.isClientSide() || this.dataStore == null) {
            return;
        }

        if (state.getBlock() != Blocks.SPAWNER) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)) {
            return;
        }

        // Spawn data
        var compound = spawnerBlockEntity.saveWithoutMetadata(level.registryAccess());
        DataStore dataStore = this.dataStore;
        dataStore.brokenSpawners.add(new MobSpawnerData(pos, compound, ((ServerLevel) level).dimension()));
        dataStore.setDirty();

        if (PCServerConfig.PUNISH_BREAKING_SPAWNER.get()) {
            this.spawnPunishment((ServerPlayer) player, (Level) level, pos, compound);
        }
    }

    private void spawnPunishment(ServerPlayer player, Level level, BlockPos spawnerPos, CompoundTag compound) {
        // EWW
        if (!compound.contains("SpawnData")) {
            return;
        }

        SpawnData spawnData = SpawnData.CODEC.parse(NbtOps.INSTANCE, compound.getCompoundOrEmpty("SpawnData")).resultOrPartial((string) -> {
            LOGGER.warn("Invalid SpawnData: {}", string);
        }).orElseGet(SpawnData::new);


        CompoundTag entityCompound = spawnData.getEntityToSpawn();

        BoundingBox box = new BoundingBox(spawnerPos)
                .inflatedBy(3);

        box = box.moved(0, spawnerPos.getY() - box.minY(), 0);

        // Flood fill the area to find connected air blocks to the spawnerPos that also have an air block above them
        List<BlockPos> airBlocks = new ArrayList<>();

        // Flood fill algorithm starting at the the spawnerPos and working outwards
        List<BlockPos> toCheck = new ArrayList<>();
        toCheck.add(spawnerPos);

        while(!toCheck.isEmpty()) {
            BlockPos currentPos = toCheck.removeFirst();
            BlockState currentState = level.getBlockState(currentPos);
            if (currentState.isAir() || currentState.canBeReplaced() || currentState.getBlock() == Blocks.SPAWNER) {
                airBlocks.add(currentPos);

                // Add adjacent blocks to check
                var nextLocations = List.of(currentPos.north(), currentPos.south(), currentPos.east(), currentPos.west(), currentPos.below(), currentPos.above());
                for (BlockPos nextLocation : nextLocations) {
                    if (!toCheck.contains(nextLocation) && !airBlocks.contains(nextLocation) && box.isInside(nextLocation)) {
                        toCheck.add(nextLocation);
                    }
                }
            }
        }

        var validBlocks = airBlocks.stream().filter(e -> e.getY() < spawnerPos.getY() + 2).toList();
        if (validBlocks.isEmpty()) {
            return;
        }

        List<BlockPos> alreadyTaken = new ArrayList<>();
        int mobsToSpawn = level.getRandom().nextInt(2, 8); // 2-8 mobs
        int tries = 0;
        while (++tries < 15 && alreadyTaken.size() < mobsToSpawn) {
            BlockPos randomPos = validBlocks.get(level.getRandom().nextInt(validBlocks.size()));
            if (alreadyTaken.contains(randomPos)) {
                continue;
            }

            alreadyTaken.add(randomPos);

            Entity entity = EntityType.loadEntityRecursive(entityCompound, level, EntitySpawnReason.COMMAND, EntityProcessor.NOP);
            if (entity == null) {
                continue;
            }

            entity.setPos(randomPos.getX() + 0.5, randomPos.getY(), randomPos.getZ() + 0.5);
            level.addFreshEntity(entity);
            var sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR);
            player.connection.send(new ClientboundSoundPacket(sound, SoundSource.AMBIENT, randomPos.getX(), randomPos.getY(), randomPos.getZ(), .3f, .4f, level.getRandom().nextInt()));
        }

    }

    private void onServerTick(LevelTickEvent event) {
        var serverLevel = event.getLevel();
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Only run this method every 10 seconds
        if (serverLevel.getGameTime() % 200 != 0) {
            return;
        }

        DataStore dataStore = this.getDataStore();
        if (dataStore == null || dataStore.brokenSpawners.isEmpty()) {
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
            if (!state.isAir() || !state.canBeReplaced()) {
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
                    foundEntity = randomEntities.get(serverLevel.getRandom().nextInt(randomEntities.size()));
                }

                var entityCompound = Util.make(new CompoundTag(), tag -> tag.put("entity",
                        Util.make(new CompoundTag(), entityTag -> entityTag.putString("id", Objects.requireNonNull(foundEntity.builtInRegistryHolder().key().identifier()).toString()))));

                compound.put("SpawnData", entityCompound);
            }

            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), compound);
            spawnerBlockEntity.loadWithComponents(valueInput);
            spawnerBlockEntity.setChanged();
            dataStore.brokenSpawners.remove(spawnerData);
            dataStore.setDirty();
        }
    }

    private int clearBrokenSpawners(CommandContext<CommandSourceStack> context) {
        var spawners = Objects.requireNonNull(dataStore).getBrokenSpawners();
        int cleared = spawners.size();
        dataStore.getBrokenSpawners().clear();
        dataStore.setDirty();
        context.getSource().sendSuccess(() -> Component.literal(cleared + " broken spawners cleared"), false);
        return 0;
    }

    @Nullable
    public DataStore getDataStore() {
        return dataStore;
    }

    public record MobSpawnerData(
       BlockPos pos,
       CompoundTag spawnerData,
       Instant breakTime,
       ResourceKey<Level> dimension
    ) {
        private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registries.DIMENSION);
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
        private static final Codec<DataStore> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobSpawnerData.CODEC.listOf().fieldOf("broken_spawners")
                        .forGetter(data -> data.brokenSpawners)
        ).apply(instance, DataStore::new));

        private static final SavedDataType<DataStore> TYPE = new SavedDataType<>(
                PackCompanion.id("spawner-manager"),
                DataStore::new,
                CODEC
        );

        private final List<MobSpawnerData> brokenSpawners;

        private DataStore() {
            this.brokenSpawners = new ArrayList<>();
        }

        private DataStore(List<MobSpawnerData> brokenSpawners) {
            this.brokenSpawners = new ArrayList<>(brokenSpawners);
        }

        public static DataStore create(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

        public List<MobSpawnerData> getBrokenSpawners() {
            return brokenSpawners;
        }
    }
}
