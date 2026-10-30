package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.server.MinecraftServer;

public interface PCServerConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-server");

    static void load(MinecraftServer server) {
        // Not needed right now
        //ConfigUtil.loadDefaulted(CONFIG, server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR), PackCompanion.MOD_ID);
    }
}
