package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;

public interface PCServerConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanionAPI.MOD_ID + "-server");

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

    BooleanValue RELOAD_PERFORMANCE = PERFORMANCE.addBoolean("reload_performance", true)
            .comment("Enable reloading performance by disabling rebuilding block cache");

    static void load(MinecraftServer server) {
        ConfigUtil.loadDefaulted(CONFIG, server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR), PackCompanionAPI.MOD_ID);
    }
}
