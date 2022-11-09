package dev.ftb.packcompanion.commands;

import com.google.common.reflect.Reflection;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class LootTableGeneratorCommand implements CommandEntry {
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create_loot_table")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(this::runCommand);
    }

    private int runCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
//        System.out.println("Hello");
//
//        ServerLevel level = commandSourceStackCommandContext.getSource().getLevel();
//
//        level.getServer().getLootTables().
//        LootTable lootTable = level.getServer().getLootTables().get(new ResourceLocation("modid", "name"));
//        ObjectArrayList<ItemStack> randomItems = lootTable.getRandomItems(new LootContext.Builder(level).create(LootContextParamSet.builder().build()));

//        try {
//            CommandSourceStack source = commandSourceStackCommandContext.getSource();
//            MinecraftServer server = source.getLevel().getServer();
//
//            LootTables lootTables = server.getLootTables();
//            List<ResourceLocation> resourceLocations = lootTables.getIds().stream().toList();
//
//            for (ResourceLocation resourceLocation : resourceLocations) {
//                LootTable lootTable = lootTables.get(resourceLocation);
//                try {
//                    Field poolsRef = lootTable.getClass().getDeclaredField("f_79109_");
//                    poolsRef.setAccessible(true);
//                    List<LootPool> pools = (List<LootPool>) poolsRef.get(lootTable);
//
//                    Field functionsRef = lootTable.getClass().getDeclaredField("functions");
//                    functionsRef.setAccessible(true);
//                    LootItemFunction[] functions = (LootItemFunction[]) functionsRef.get(lootTable);
//
//                    for (LootPool pool : pools) {
//                        pool.
//                    }
//
//                    System.out.println(pools);
//                    System.out.println(Arrays.toString(functions));
//                } catch (NoSuchFieldException | IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return 1;
    }
}
