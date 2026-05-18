package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringListValue;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.config.values.AbstractMapValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface PCServerConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanionAPI.MOD_ID + "-server");

    SNBTConfig SPAWNERS = CONFIG.addGroup("spawners");
    BooleanValue SPAWNERS_ALLOW_RESPAWN = SPAWNERS.addBoolean("allow_respawn", false)
            .comment("When enabled, broken spawner blocks will be remembered and will respawn at a given interval.");

    IntValue SPAWNERS_RESPAWN_INTERVAL = SPAWNERS.addInt("respawn_interval", 60, 0, 24 * 60)
            .comment("The interval in minutes at which spawners will respawn.");

    StringListValue SPAWNERS_USE_RANDOM_ENTITY = SPAWNERS.addStringList("random_entity", new ArrayList<>())
            .comment("A list of entity types that will be used to replace broken spawners. Set to an empty list to disable.");

    BooleanValue PUNISH_BREAKING_SPAWNER = SPAWNERS.addBoolean("punish_for_breaking_spawners", false);

    DoubleValue MODIFY_MOB_BASE_HEALTH = CONFIG.addDouble("modify_mob_base_health", 0D, 0D, 1000D)
            .comment("If non-zero, set the base health of all mobs to be multiplied by this value. Set to 0 to disable.");

    SNBTConfig VILLAGERS = CONFIG.addGroup("villagers");
    BooleanValue NO_WANDERING_TRADER_INVIS_POTIONS = VILLAGERS.addBoolean("no_wandering_trader_invis_potions", false)
            .comment("If true, Wandering Traders will no longer drink invisibility potions at night",
                    "(or milk buckets to remove their invisibility when it's day)");

    SNBTConfig SCHEMATICS = CONFIG.addGroup("schematics");
    IntValue GLOBAL_PASTE_LIMIT = SCHEMATICS.addInt("global_paste_limit", 0)
            .comment("Maximum number of blocks/tick that can be pasted, divided equally among all current paste workers",
                    "A value of 0 indicates no limit");

    BooleanValue SCHEMATIC_SEAL_PERIMETER = SCHEMATICS.addBoolean("seal_perimeter", true)
            .comment("If true, before pasting begins the worker wraps the schematic bounding box in a 1-block-thick",
                    "shell of bedrock. This prevents water/lava/falling-blocks from neighbouring terrain flowing into",
                    "the in-progress paste while it runs (chunk-by-chunk pastes can take many minutes).");

    BooleanValue SCHEMATIC_CLEANUP_FLUIDS = SCHEMATICS.addBoolean("cleanup_fluids", true)
            .comment("If true, after paste finishes the worker scans the schematic bounding box and replaces any",
                    "water/lava found at positions where the schematic itself contained air. This mops up fluid that",
                    "leaked in during paste. Has no effect on cells where the schematic intentionally placed fluid.");

    BooleanValue SCHEMATIC_CLEANUP_FALLING_BLOCKS = SCHEMATICS.addBoolean("cleanup_falling_blocks", false)
            .comment("If true, the post-paste cleanup also removes sand/gravel/concrete-powder etc. that fell in from",
                    "above the bounding box. Disabled by default since some schematics use falling blocks deliberately.");

    BooleanValue SCHEMATIC_REMOVE_SHELL_AFTER_PASTE = SCHEMATICS.addBoolean("remove_shell_after_paste", true)
            .comment("If true, the bedrock shell from seal_perimeter is removed during cleanup (replaced with air).",
                    "If false, the shell remains permanently — fully bulletproof against later fluid intrusion but",
                    "visible as a bedrock cage around the structure.");

    IntValue SCHEMATIC_CLEANUP_SCAN_MULTIPLIER = SCHEMATICS.addInt("cleanup_scan_multiplier", 20, 1, 1000)
            .comment("Cleanup scans many cells per tick but writes few (most cells are schematic-solid and skip).",
                    "This multiplier sets how many cells are scanned per paste-budget unit. 20 means a 200 blocks/tick",
                    "paste rate scans 4000 cells/tick during cleanup. Raise for faster cleanup, lower for less tick load.");

    AbstractMapValue.CodecBased<GameType> DIMENSION_FORCED_GAMEMODES = CONFIG.add(new AbstractMapValue.CodecBased<>(
            CONFIG,
            "dimension_forced_gamemodes",
            new HashMap<>(Map.of(
                "ftb:test_dimension", GameType.CREATIVE
            )),
            GameType.CODEC
    ).comment("A mapping of dimension IDs to forced game modes. Players entering the dimension will have their game mode changed accordingly."));

    static void load(MinecraftServer server) {
        ConfigUtil.loadDefaulted(CONFIG, server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR), PackCompanionAPI.MOD_ID);
    }
}
