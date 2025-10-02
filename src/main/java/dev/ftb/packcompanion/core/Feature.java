package dev.ftb.packcompanion.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public abstract class Feature {
    public Feature(IEventBus modEventBus, ModContainer container) {
    }

    public void onReload(ResourceManager resourceManager) {}

    public void onDataGather(DataGatherCollector collector) {

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

        public void registerPackets(PayloadRegistrar registrar) {

        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> getRegistry(ResourceKey<? extends Registry<T>> registry) {
        return (DeferredRegister<T>) PackCompanion.REGISTRIES.computeIfAbsent(registry,
                (key) -> DeferredRegister.create(registry, PackCompanion.MOD_ID)
        );
    }
}
