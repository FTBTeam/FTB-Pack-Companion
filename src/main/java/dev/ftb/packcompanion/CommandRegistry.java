package dev.ftb.packcompanion;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.commands.CommandEntry;
import dev.ftb.packcompanion.commands.SpawnerManagerClearCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Set;

public class CommandRegistry {
    private static final Set<CommandEntry> COMMANDS = Set.of(
//        new LootTableGeneratorCommand()
        new SpawnerManagerClearCommand()
    );

    public static void setup(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        LiteralArgumentBuilder<CommandSourceStack> companionRootCommand = Commands.literal(PackCompanionAPI.MOD_ID);

        for (CommandEntry command : COMMANDS) {
            companionRootCommand.then(command.register());
        }

        commandDispatcher.register(companionRootCommand);
    }
}
