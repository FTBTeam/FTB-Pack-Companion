package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import dev.ftb.packcompanion.api.PackCompanionAPI;

public interface PCClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanionAPI.MOD_ID + "-client");

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

    BooleanValue REMOVE_ADVANCEMENTS_FROM_PAUSE = CONFIG.getBoolean("remove_advancements_from_pause", false)
            .comment("When enabled, the advancements button will be removed from the pause menu.");

    SNBTConfig PAUSE_SCREEN = CONFIG.getGroup("pause_screen");

    BooleanValue ENABLE_SUPPORT_PROVIDER = PAUSE_SCREEN.getBoolean("enable_support_provider", true)
            .comment("When enabled, the support provider will be enabled in the top left of the pause screen. This is used for modpacks to provide support information.");

    StringValue SUPPORT_GITHUB_URL = PAUSE_SCREEN.getString("support_github_url", "https://go.ftb.team/support-modpack")
            .comment("The URL to open when the support provider's GitHub icon is clicked. If this is empty, the GitHub icon will not be shown.");

    StringValue SUPPORT_DISCORD_URL = PAUSE_SCREEN.getString("support_discord_url", "https://go.ftb.team/discord")
            .comment("The URL to open when the support provider's Discord icon is clicked. If this is empty, the Discord icon will not be shown.");

    static void load() {
        ConfigUtil.loadDefaulted(CONFIG, PackCompanionExpectPlatform.getConfigDirectory(), PackCompanionAPI.MOD_ID);
    }
}
