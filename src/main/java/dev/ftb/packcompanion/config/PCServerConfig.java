package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.packcompanion.PackCompanion;

public interface PCServerConfig {
    Config CONFIG = Config.create(PackCompanion.MOD_ID + "-server");

    static void init() {
//        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".server", false);
    }
}
