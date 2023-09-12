package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public interface PCServerConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-server");

    SNBTConfig SPAWNERS = CONFIG.getGroup("spawners");
    BooleanValue SPAWNERS_ALLOW_RESPAWN = SPAWNERS.getBoolean("allow_respawn", false)
            .comment("When enabled, broken spawner blocks will be remembered and will respawn at a given interval.");

    IntValue SPAWNERS_RESPAWN_INTERVAL = SPAWNERS.getInt("respawn_interval", 60, 0, 24 * 60)
            .comment("The interval in minutes at which spawners will respawn. Set to 0 to disable.");

    StringListValue SPAWNERS_USE_RANDOM_ENTITY = SPAWNERS.getStringList("random_entity", new ArrayList<>())
            .comment("A list of entity types that will be used to replace broken spawners. Set to an empty list to disable.");

    static void load(MinecraftServer server) {
        ConfigUtil.loadDefaulted(CONFIG, server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR), PackCompanion.MOD_ID);
    }
}
