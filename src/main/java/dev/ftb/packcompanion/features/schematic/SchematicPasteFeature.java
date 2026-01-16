package dev.ftb.packcompanion.features.schematic;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;

import java.util.List;

public class SchematicPasteFeature extends Feature.Server {
    public SchematicPasteFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }

    @Override
    public void onServerInit(MinecraftServer server) {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @Override
    public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        return List.of(SchematicCommand.register());
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SchematicPasteManager.getInstance(event.getServer()).tick(event.getServer());
        }
    }
}
