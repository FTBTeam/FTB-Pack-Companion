package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;

public interface PCCommonConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-common");

    BooleanValue ALLOW_BEDS_IN_THE_NETHER = CONFIG.getBoolean("allow_beds_in_the_nether", false)
            .comment("When enabled, players will be able to use beds in the nether.");
    BooleanValue EXTENDED_JIGSAW_RANGE = CONFIG.getBoolean("extended_jigsaw_range", true)
            .comment("When enabled, expand jigsaw feature size limit from 128 to 256.");

    static void load() {
        ConfigUtil.loadDefaulted(CONFIG, PackCompanionExpectPlatform.getConfigDirectory(), PackCompanion.MOD_ID);
    }
}
