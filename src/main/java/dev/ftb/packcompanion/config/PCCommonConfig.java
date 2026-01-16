package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringMapValue;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.config.values.ChunkPosCustomYHashValue;
import dev.ftb.packcompanion.config.values.GameRuleMapping;
import dev.ftb.packcompanion.config.values.SparseStructuresConfig;
import dev.ftb.packcompanion.config.values.SparseStructuresValue;

import java.util.*;

public interface PCCommonConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-common");

    BooleanValue BREAK_LIGHT_SOURCES_NEAR_SPAWNERS = CONFIG.addBoolean("break_light_sources_near_spawners", false)
            .comment("When enabled, light sources near spawners will be broken to allow for mob spawning.");

    BooleanValue IGNORE_LIGHT_LEVEL_FOR_SPAWNERS = CONFIG.addBoolean("ignore_light_level_for_spawners", false)
            .comment("When enabled, the light level around spawners will be ignored for mob spawning.");

    BooleanValue REMOVE_CONCENTRIC_RING_PLACEMENT_BIAS = CONFIG.addBoolean("remove_concentric_ring_placement_bias", false)
            .comment("When enabled, the random bias applied to concentric ring distances is removed");

    SparseStructuresValue SPARSE_STRUCTURES = CONFIG.add(new SparseStructuresValue(CONFIG, "sparse_structures", SparseStructuresConfig.DEFAULT));

    SNBTConfig SHADERS_NOTICE = CONFIG.addGroup("shaders_notice");

    StringValue SHADER_PACK_TO_USE = SHADERS_NOTICE.addString("shader_pack_to_use", "")
            .comment("The shader pack to use when enabling shaders. Leave empty to use the default shader pack / first available shader pack in the list");

    BooleanValue SHOW_ON_START = SHADERS_NOTICE.addBoolean("show_on_start", false)
            .comment("When enabled, the shaders notice will be shown on world start if shaders are included in the pack.");

    SNBTConfig INTEGRATIONS = CONFIG.addGroup("integrations");

    SNBTConfig FTB_CHUNKS = INTEGRATIONS.addGroup("ftb_chunks")
            .comment("Configuration options for the FTB Chunks mod integration. Only effective if FTB Chunks is installed.");

    ChunkPosCustomYHashValue CUSTOM_Y_LEVEL_CHUNK_POSITIONS = FTB_CHUNKS.add(new ChunkPosCustomYHashValue(
            FTB_CHUNKS,
            "custom_y_level_chunk_positions",
            new ArrayList<>()
    ).comment("Custom min-y level list for specific block locations within a range from the center, per dimension."));

    SNBTConfig FORCED_GAME_RULES = CONFIG.addGroup("forced-game-rules");

    GameRuleMapping GAME_RULE_MAPPING = FORCED_GAME_RULES.add(new GameRuleMapping(
            FORCED_GAME_RULES,
            "rules",
            Map.of()
    ).comment("Game rules that are forced on the server. The keys must be valid game rule IDs."));

    SNBTConfig STRUCTURE_PLACER = CONFIG.addGroup("structure_placer");

    StringMapValue STRUCTURE_PLACER_TEMPLATES = STRUCTURE_PLACER.add(new StringMapValue(
            STRUCTURE_PLACER,
            "templates",
            Map.of()
    ).comment("Structure templates available for the Structure Placer feature. The key is the template name, and the value is the resource location of the structure."));

    static void init() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".common", true);
    }

}
