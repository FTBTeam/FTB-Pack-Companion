package dev.ftb.packcompanion.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface CommandEntry {
    LiteralArgumentBuilder<CommandSourceStack> register();
}
