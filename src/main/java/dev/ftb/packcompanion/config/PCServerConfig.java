package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.packcompanion.PackCompanion;

public interface PCServerConfig {
    Config CONFIG = Config.create(PackCompanion.MOD_ID + "-server");

    SNBTConfig CMD_FEEDBACK_FILTERING = CONFIG.addGroup("command_feedback_filtering");

    StringListValue BLACKLISTED_OPS = CMD_FEEDBACK_FILTERING.addStringList("blacklisted_ops", new ArrayList<>())
            .comment("A list of operator who's command usage feedback will be hidden from other operators. This is a list of UUIDs. If this list is empty, the feature is disabled.");

    StringListValue HIDDEN_COMMAND_FEEDBACK_KEYS = CMD_FEEDBACK_FILTERING.addStringList("hidden_command_feedback_keys", new ArrayList<>())
            .comment("A list of command feedback keys that will be hidden from operators. This is a list of translation keys which are checked based on their prefix. This means that an entry like commands.give.success would match commands.give.success.single");

    static void init() {
//        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".server", false);
    }
}
