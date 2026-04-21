package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.config.values.GameRuleMapping;
import net.minecraft.world.level.GameType;

import java.util.*;

public interface PCCommonConfig {
    Config CONFIG = Config.create(PackCompanion.MOD_ID + "-common");

    //#region Spawners
    Config SPAWNERS = CONFIG.addGroup("spawners")
            .comment("Settings related to mob spawners.");

    BooleanValue BREAK_LIGHT_SOURCES_NEAR_SPAWNERS = SPAWNERS
            .addBoolean("break_light_sources_near_spawners", false)
            .comment("When enabled, light sources near spawners will be broken to allow for mob spawning.");

    BooleanValue IGNORE_LIGHT_LEVEL_FOR_SPAWNERS = SPAWNERS
            .addBoolean("ignore_light_level_for_spawners", false)
            .comment("When enabled, the light level around spawners will be ignored for mob spawning.");

    BooleanValue SPAWNERS_ALLOW_RESPAWN = SPAWNERS
            .addBoolean("allow_respawn", false)
            .comment("When enabled, broken spawner blocks will be remembered and will respawn at a given interval.");

    IntValue SPAWNERS_RESPAWN_INTERVAL = SPAWNERS
            .addInt("respawn_interval", 60, 0, 24 * 60)
            .comment("The interval in minutes at which spawners will respawn.");

    StringListValue SPAWNERS_USE_RANDOM_ENTITY = SPAWNERS
            .addStringList("random_entity", new ArrayList<>())
            .comment("A list of entity types that will be used to replace broken spawners. Set to an empty list to disable.");

    BooleanValue PUNISH_BREAKING_SPAWNER = SPAWNERS
            .addBoolean("punish_for_breaking_spawners", false)
            .comment("Apply a punishment effect when a player breaks a spawner.");
    //#endregion

    //#region Shaders notice
    Config SHADERS_NOTICE = CONFIG.addGroup("shaders_notice");

    StringValue SHADER_PACK_TO_USE = SHADERS_NOTICE
            .addString("shader_pack_to_use", "")
            .comment("The shader pack to use when enabling shaders. Leave empty to use the default shader pack / first available shader pack in the list");

    BooleanValue SHOW_ON_START = SHADERS_NOTICE
            .addBoolean("show_on_start", false)
            .comment("When enabled, the shaders notice will be shown on world start if shaders are included in the pack.");
    //#endregion

    Config WORLDGEN = CONFIG.addGroup("worldgen")
            .comment("Settings related to world generation.");

    StringMapValue STRUCTURE_ROTATION_OVERRIDE = WORLDGEN.add(new StringMapValue(WORLDGEN, "structure_rotation_override", new HashMap<>()))
            .comment("Applies to structures of type 'minecraft:jigsaw' only",
                    "Maps template pool ID's to a forced rotation for that pool: one of 'none', 'clockwise_90', '180', 'counterclockwise_90'");

    Config MISC = CONFIG.addGroup("misc")
            .comment("Miscellaneous settings that don't fit in other categories.");

    BooleanValue NO_WANDERING_TRADER_INVIS_POTIONS = MISC.addBoolean("no_wandering_trader_invis_potions", false)
            .comment("If true, Wandering Traders will no longer drink invisibility potions at night",
                    "(or milk buckets to remove their invisibility when it's day)");

    AbstractMapValue<GameType> DIMENSION_FORCED_GAME_MODES = MISC.add(new AbstractMapValue<>(
            MISC,
            "dimension_forced_gamemodes",
            new HashMap<>(),
            GameType.CODEC
    ) {}.comment("A mapping of dimension IDs to forced game modes. Players entering the dimension will have their game mode changed accordingly."));

    GameRuleMapping GAME_RULE_MAPPING = MISC.add(new GameRuleMapping(
            MISC,
            "forced_game_rules"
    )
            .excludedFromGui()
            .comment("Game rules that are forced on the server. The keys must be valid game rule IDs."));

    static void init() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".common", true);
    }

}
