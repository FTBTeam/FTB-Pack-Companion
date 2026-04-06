package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;

public interface PCServerConfig {
    Config CONFIG = Config.create(PackCompanion.MOD_ID + "-server");

    Config SPAWNERS = CONFIG.addGroup("spawners");
    BooleanValue SPAWNERS_ALLOW_RESPAWN = SPAWNERS.addBoolean("allow_respawn", false)
            .comment("When enabled, broken spawner blocks will be remembered and will respawn at a given interval.");

    IntValue SPAWNERS_RESPAWN_INTERVAL = SPAWNERS.addInt("respawn_interval", 60, 0, 24 * 60)
            .comment("The interval in minutes at which spawners will respawn.");

    StringListValue SPAWNERS_USE_RANDOM_ENTITY = SPAWNERS.addStringList("random_entity", new ArrayList<>())
            .comment("A list of entity types that will be used to replace broken spawners. Set to an empty list to disable.");

    BooleanValue PUNISH_BREAKING_SPAWNER = SPAWNERS.addBoolean("punish_for_breaking_spawners", false);

    Config WORLDGEN = CONFIG.addGroup("worldgen");
    StringMapValue STRUCTURE_ROTATION_OVERRIDE = WORLDGEN.add(new StringMapValue(WORLDGEN, "structure_rotation_override", new HashMap<>()))
            .comment("Applies to structures of type 'minecraft:jigsaw' only",
                    "Maps template pool ID's to a forced rotation for that pool: one of 'none', 'clockwise_90', '180', 'counterclockwise_90'");

    Config VILLAGERS = CONFIG.addGroup("villagers");
    BooleanValue NO_WANDERING_TRADER_INVIS_POTIONS = VILLAGERS.addBoolean("no_wandering_trader_invis_potions", false)
            .comment("If true, Wandering Traders will no longer drink invisibility potions at night",
                    "(or milk buckets to remove their invisibility when it's day)");

    AbstractMapValue<GameType> DIMENSION_FORCED_GAMEMODES = CONFIG.add(new AbstractMapValue<>(
            CONFIG,
            "dimension_forced_gamemodes",
            new HashMap<>(),
            GameType.CODEC
    ) {}.comment("A mapping of dimension IDs to forced game modes. Players entering the dimension will have their game mode changed accordingly."));

    static void init() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".server", false);
    }
}
