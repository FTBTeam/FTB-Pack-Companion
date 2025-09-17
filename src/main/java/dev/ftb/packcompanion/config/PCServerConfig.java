package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.packcompanion.PackCompanion;

import java.util.ArrayList;
import java.util.HashMap;

public interface PCServerConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-server");

    SNBTConfig SPAWNERS = CONFIG.addGroup("spawners");
    BooleanValue SPAWNERS_ALLOW_RESPAWN = SPAWNERS.addBoolean("allow_respawn", false)
            .comment("When enabled, broken spawner blocks will be remembered and will respawn at a given interval.");

    IntValue SPAWNERS_RESPAWN_INTERVAL = SPAWNERS.addInt("respawn_interval", 60, 0, 24 * 60)
            .comment("The interval in minutes at which spawners will respawn.");

    StringListValue SPAWNERS_USE_RANDOM_ENTITY = SPAWNERS.addStringList("random_entity", new ArrayList<>())
            .comment("A list of entity types that will be used to replace broken spawners. Set to an empty list to disable.");

    BooleanValue PUNISH_BREAKING_SPAWNER = SPAWNERS.addBoolean("punish_for_breaking_spawners", false);

    DoubleValue MODIFY_MOB_BASE_HEALTH = CONFIG.addDouble("modify_mob_base_health", 0D, 0D, 1000D)
            .comment("If non-zero, set the base health of all mobs to be multiplied by this value. Set to 0 to disable.");

    SNBTConfig PERFORMANCE = CONFIG.addGroup("performance");

    BooleanValue RELOAD_PERFORMANCE = PERFORMANCE.addBoolean("skip_block_cache_rebuild", true)
            .comment("Improve reloading performance by disabling block cache rebuild on client tag data reload");

    SNBTConfig WORLDGEN = CONFIG.addGroup("worldgen");
    StringMapValue STRUCTURE_ROTATION_OVERRIDE = WORLDGEN.add(new StringMapValue(WORLDGEN, "structure_rotation_override", new HashMap<>()))
            .comment("Applies to structures of type 'minecraft:jigsaw' only",
                    "Maps template pool ID's to a forced rotation for that pool: one of 'none', 'clockwise_90', '180', 'counterclockwise_90'");

    static void init() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".server", false);
    }
}
