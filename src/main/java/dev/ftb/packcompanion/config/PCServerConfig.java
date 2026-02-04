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
