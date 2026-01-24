package dev.ftb.packcompanion.features.schematic;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

public class SchematicPasteFeature extends Feature.Server {
    public SchematicPasteFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }

    @Override
    public void onServerInit(MinecraftServer server) {
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @Override
    public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        return List.of(SchematicCommand.register());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        SchematicPasteManager.getInstance(event.getServer()).tick(event.getServer());
    }
}
