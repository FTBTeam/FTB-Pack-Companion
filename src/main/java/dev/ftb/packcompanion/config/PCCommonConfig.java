package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.config.values.GameRuleMapping;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.Map;

public interface PCCommonConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanionAPI.MOD_ID + "-common");

    BooleanValue ALLOW_BEDS_IN_THE_NETHER = CONFIG.addBoolean("allow_beds_in_the_nether", false)
            .comment("When enabled, players will be able to use beds in the nether.");
    IntValue EXTENDED_JIGSAW_RANGE = CONFIG.addInt("extended_jigsaw_range", 0, 0, 256)
            .comment("If non-zero, set the jigsaw feature size limit to this value.");

    BooleanValue BREAK_LIGHT_SOURCES_NEAR_SPAWNERS = CONFIG.addBoolean("break_light_sources_near_spawners", false)
            .comment("When enabled, light sources near spawners will be broken to allow for mob spawning.");

    BooleanValue IGNORE_LIGHT_LEVEL_FOR_SPAWNERS = CONFIG.addBoolean("ignore_light_level_for_spawners", false)
            .comment("When enabled, the light level around spawners will be ignored for mob spawning.");

    SNBTConfig FORCED_GAME_RULES = CONFIG.addGroup("forced-game-rules");

    GameRuleMapping GAME_RULE_MAPPING = FORCED_GAME_RULES.add(new GameRuleMapping(
            FORCED_GAME_RULES,
            "rules",
            Map.of()
    ).comment("Game rules that are forced on the server. The keys must be valid game rule IDs."));

    SNBTConfig SHADERS_NOTICE = CONFIG.addGroup("shaders_notice");

    StringValue SHADER_PACK_TO_USE = SHADERS_NOTICE.addString("shader_pack_to_use", "")
            .comment("The shader pack to use when enabling shaders. Leave empty to use the default shader pack / first available shader pack in the list");

    BooleanValue SHOW_ON_START = SHADERS_NOTICE.addBoolean("show_on_start", false)
            .comment("When enabled, the shaders notice will be shown on world start if shaders are included in the pack.");


    static void load() {
        ConfigUtil.loadDefaulted(CONFIG, FMLPaths.CONFIGDIR.get(), PackCompanionAPI.MOD_ID);
    }
}
