package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;

public interface PCClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-client");

    BooleanValue DISABLE_TUTORIAL_TOASTS = CONFIG.getBoolean("disable_tutorial_toasts", true)
            .comment("When enabled, toasts regarding the in-game start tutorial will be disabled.");

    BooleanValue DISABLE_ADVANCEMENT_TOASTS = CONFIG.getBoolean("disable_advancements_toasts", false)
            .comment("When enabled, toasts regarding the advancement progression will be disabled.");

    BooleanValue DISABLE_RECIPE_TOASTS = CONFIG.getBoolean("disable_recipe_toasts", false)
            .comment("When enabled, toasts regarding the recipe unlocks will be disabled.");

    BooleanValue WORLD_USES_STATIC_SEED = CONFIG.getBoolean("world_uses_static_seed", false)
            .comment("When enabled, the world will always use the same seed, regardless of the world name.");

    StringValue STATIC_SEED = CONFIG.getString("static_seed", "")
            .comment("The seed to use for the world. Only used if world_uses_static_seed is enabled.");

    static void load() {
        ConfigUtil.loadDefaulted(CONFIG, PackCompanionExpectPlatform.getConfigDirectory(), PackCompanion.MOD_ID);
    }
}
