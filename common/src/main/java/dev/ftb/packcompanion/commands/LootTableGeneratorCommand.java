package dev.ftb.packcompanion.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootTableGeneratorCommand implements CommandEntry {
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create_loot_table")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(this::runCommand);
    }

    private int runCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        System.out.println("Hello");

        ServerLevel level = commandSourceStackCommandContext.getSource().getLevel();

        level.getServer().getLootTables().
        LootTable lootTable = level.getServer().getLootTables().get(new ResourceLocation("modid", "name"));
        ObjectArrayList<ItemStack> randomItems = lootTable.getRandomItems(new LootContext.Builder(level).create(LootContextParamSet.builder().build()));

        return 1;
    }
}
