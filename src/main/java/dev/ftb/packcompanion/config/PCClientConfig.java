package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.StringValue;
import dev.ftb.packcompanion.PackCompanion;

public interface PCClientConfig {
    Config CONFIG = Config.create(PackCompanion.MOD_ID + "-client");

    Config TOASTS = CONFIG.addGroup("toasts")
            .comment("Settings related to in-game toasts.");

    BooleanValue DISABLE_TUTORIAL_TOASTS = TOASTS.addBoolean("disable_tutorial_toasts", true)
            .comment("When enabled, toasts regarding the in-game start tutorial will be disabled.");

    BooleanValue DISABLE_ADVANCEMENT_TOASTS = TOASTS.addBoolean("disable_advancements_toasts", false)
            .comment("When enabled, toasts regarding the advancement progression will be disabled.");

    BooleanValue DISABLE_RECIPE_TOASTS = TOASTS.addBoolean("disable_recipe_toasts", false)
            .comment("When enabled, toasts regarding the recipe unlocks will be disabled.");

    BooleanValue DISABLE_SOCIALINTERACTION_TOASTS = TOASTS.addBoolean("disable_socialinteraction_toasts", false)
            .comment("When enabled, toasts regarding social interaction will be disabled.");

    Config WORLDGEN = CONFIG.addGroup("worldgen")
            .comment("Settings related to world generation.");

    StringValue STATIC_SEED = WORLDGEN.addString("static_seed", "")
            .comment("The seed to use for the world. Leave empty to use a random seed like vanilla.");

    static void init() {
        ConfigManager.getInstance().registerClientConfig(CONFIG, PackCompanion.MOD_ID + ".client");
    }
}
