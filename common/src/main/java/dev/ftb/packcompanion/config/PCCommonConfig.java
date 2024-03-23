package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import dev.ftb.packcompanion.api.PackCompanionAPI;

public interface PCCommonConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanionAPI.MOD_ID + "-common");

    BooleanValue ALLOW_BEDS_IN_THE_NETHER = CONFIG.getBoolean("allow_beds_in_the_nether", false)
            .comment("When enabled, players will be able to use beds in the nether.");
    IntValue EXTENDED_JIGSAW_RANGE = CONFIG.getInt("extended_jigsaw_range", 0, 0, 256)
            .comment("If non-zero, set the jigsaw feature size limit to this value.");

    BooleanValue BREAK_LIGHT_SOURCES_NEAR_SPAWNERS = CONFIG.getBoolean("break_light_sources_near_spawners", false)
            .comment("When enabled, light sources near spawners will be broken to allow for mob spawning.");

    BooleanValue IGNORE_LIGHT_LEVEL_FOR_SPAWNERS = CONFIG.getBoolean("ignore_light_level_for_spawners", false)
            .comment("When enabled, the light level around spawners will be ignored for mob spawning.");

    static void load() {
        ConfigUtil.loadDefaulted(CONFIG, PackCompanionExpectPlatform.getConfigDirectory(), PackCompanionAPI.MOD_ID);
    }
}
