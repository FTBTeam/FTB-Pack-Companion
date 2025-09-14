package dev.ftb.packcompanion;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.buffs.MobEntityBuffFeature;
import dev.ftb.packcompanion.features.events.EventSystem;
import dev.ftb.packcompanion.features.loot.RandomNameLootFeature;
import dev.ftb.packcompanion.features.onboarding.shadernotice.ShaderNotice;
import dev.ftb.packcompanion.features.spawners.SpawnerFeature;
import dev.ftb.packcompanion.features.structures.StructuresFeature;
import dev.ftb.packcompanion.integrations.Integrations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod(PackCompanion.MOD_ID)
public class PackCompanion {
    public static final String MOD_ID = "ftbpc";

    public static final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> REGISTRIES = new HashMap<>();

    private static final List<BiFunction<IEventBus, ModContainer, Feature>> FEATURES = List.of(
            RandomNameLootFeature::new,
            MobEntityBuffFeature::new,
            SpawnerFeature::new,
            StructuresFeature::new,
            ShaderNotice::new,
            EventSystem::new
    );

    private final List<Feature> createdFeatures = new ArrayList<>();

    public PackCompanion(IEventBus modEventBus, ModContainer container) {
        // Set up the features
        FEATURES.forEach(featureConstructor -> {
            Feature feature = featureConstructor.apply(modEventBus, container);
            createdFeatures.add(feature);
        });

        modEventBus.addListener(this::onSetup);
        modEventBus.addListener(this::onClientInit);
        modEventBus.addListener(this::registerNetwork);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverBeforeStart);
        NeoForge.EVENT_BUS.addListener(this::serverStarted);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
    }

    private void onClientInit(FMLClientSetupEvent event) {
        PackCompanionClient.init(createdFeatures.stream().filter(e -> e instanceof Feature.Client).map(e -> (Feature.Client) e).toList());
    }

    private void onSetup(FMLCommonSetupEvent event) {
        PCCommonConfig.load();
        Integrations.commonInit();

        runForFeatures(feature -> feature instanceof Feature.Common, feature -> ((Feature.Common) feature).onCommonInit());
    }

    private void serverBeforeStart(ServerAboutToStartEvent event) {
        PCServerConfig.load();
        Integrations.serverInit();
    }

    private void serverStarted(ServerStartedEvent event) {
        runForFeatures(feature -> feature instanceof Feature.Server, feature -> ((Feature.Server) feature).onServerInit(event.getServer()));
    }

    private void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> companionRootCommand = Commands.literal(PackCompanion.MOD_ID);

        runForFeatures(
                feature -> feature instanceof Feature.Common || feature instanceof Feature.Server,
                feature -> {
                    List<LiteralArgumentBuilder<CommandSourceStack>> commands = feature instanceof Feature.Common
                            ? ((Feature.Common) feature).commands(event.getBuildContext(), event.getCommandSelection())
                            : ((Feature.Server) feature).commands(event.getBuildContext(), event.getCommandSelection());

                    commands.forEach(companionRootCommand::then);
                }
        );

        event.getDispatcher().register(companionRootCommand);
    }

    public void registerNetwork(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        runForFeatures(
                feature -> feature instanceof Feature.Common,
                feature -> ((Feature.Common) feature).registerPackets(registrar)
        );
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ServerDataReloader());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private class ServerDataReloader implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            runForFeatures(
                    feature -> feature instanceof Feature.Common || feature instanceof Feature.Server,
                    feature -> feature.onReload(resourceManager)
            );
        }
    }

    private void runForFeatures(Predicate<Feature> test, Consumer<Feature> action) {
        for (Feature feature : createdFeatures) {
            if (!test.test(feature)) {
                continue;
            }

            action.accept(feature);
        }
    }
}
