package dev.ftb.packcompanion;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.commands.CommandEntry;
import dev.ftb.packcompanion.commands.LootTableGeneratorCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Set;

public class CommandRegistry {
    private static final Set<CommandEntry> COMMANDS = Set.of(
        new LootTableGeneratorCommand()
    );

    public static void setup(CommandDispatcher<CommandSourceStack> commandDispatcher, Commands.CommandSelection commandSelection) {
//        LiteralArgumentBuilder<CommandSourceStack> companionRootCommand = Commands.literal(PackCompanion.MOD_ID);
//
//        for (CommandEntry command : COMMANDS) {
//            companionRootCommand.then(command.register());
//        }
//
//        commandDispatcher.register(companionRootCommand);
    }
}
