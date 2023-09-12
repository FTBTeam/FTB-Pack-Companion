package dev.ftb.packcompanion.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.packcompanion.features.spawners.SpawnerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SpawnerManagerClearCommand implements CommandEntry {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawner_manager").then(Commands.literal("clear").executes(this::clear));
    }

    private int clear(CommandContext<CommandSourceStack> context) {
        SpawnerManager.DataStore dataStore = SpawnerManager.get().getDataStore();
        var spawners = dataStore.getBrokenSpawners();
        dataStore.getBrokenSpawners().clear();
        dataStore.setDirty();
        context.getSource().sendSuccess(Component.literal(spawners.size() + " broken spawners cleared"), false);
        return 0;
    }
}
