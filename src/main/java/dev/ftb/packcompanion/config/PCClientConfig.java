package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.packcompanion.PackCompanion;

public interface PCClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-client");

    BooleanValue DISABLE_TUTORIAL_TOASTS = CONFIG.addBoolean("disable_tutorial_toasts", true)
            .comment("When enabled, toasts regarding the in-game start tutorial will be disabled.");

    BooleanValue DISABLE_ADVANCEMENT_TOASTS = CONFIG.addBoolean("disable_advancements_toasts", false)
            .comment("When enabled, toasts regarding the advancement progression will be disabled.");

    BooleanValue DISABLE_RECIPE_TOASTS = CONFIG.addBoolean("disable_recipe_toasts", false)
            .comment("When enabled, toasts regarding the recipe unlocks will be disabled.");

    BooleanValue DISABLE_SOCIALINTERACTION_TOASTS = CONFIG.addBoolean("disable_socialinteraction_toasts", false)
            .comment("When enabled, toasts regarding social interaction will be disabled.");

    BooleanValue WORLD_USES_STATIC_SEED = CONFIG.addBoolean("world_uses_static_seed", false)
            .comment("When enabled, the world will always use the same seed, regardless of the world name.");

    StringValue STATIC_SEED = CONFIG.addString("static_seed", "")
            .comment("The seed to use for the world. Only used if world_uses_static_seed is enabled.");

    SNBTConfig PERFORMANCE = CONFIG.addGroup("performance");

    BooleanValue RELOAD_PERFORMANCE = PERFORMANCE.addBoolean("skip_block_cache_rebuild", true)
            .comment("Improve reloading performance by disabling block cache rebuild on server resource reload");

    static void init() {
        ConfigManager.getInstance().registerClientConfig(CONFIG, PackCompanion.MOD_ID + ".client");
    }
}
