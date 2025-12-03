package dev.ftb.packcompanion.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Feature {
    private final IEventBus modEventBus;
    private final ModContainer container;

    public Feature(IEventBus modEventBus, ModContainer container) {
        this.modEventBus = modEventBus;
        this.container = container;
    }

    public void onReload(ResourceManager resourceManager) {}

    public void onDataGather(DataGatherCollector collector) {

    }

    public IEventBus modEventBus() {
        return modEventBus;
    }

    public ModContainer container() {
        return container;
    }

    public static abstract class Client extends Feature {
        public Client(IEventBus modEventBus, ModContainer container) {
            super(modEventBus, container);
        }

        public void onClientInit() {}
    }

    public static abstract class Server extends Feature {
        public Server(IEventBus modEventBus, ModContainer container) {
            super(modEventBus, container);
        }

        public void onServerInit(MinecraftServer server) {

        }

        public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
            return List.of();
        }
    }

    public static abstract class Common extends Feature {
        public Common(IEventBus modEventBus, ModContainer container) {
            super(modEventBus, container);
        }

        public void onCommonInit() {

        }

        public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
            return List.of();
        }

        public void registerPackets(SimpleChannel channel, AtomicInteger packetId) {

        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> getRegistry(ResourceKey<? extends Registry<T>> registry) {
        return (DeferredRegister<T>) PackCompanion.REGISTRIES.computeIfAbsent(registry,
                (key) -> DeferredRegister.create(registry, PackCompanionAPI.MOD_ID)
        );
    }
}
