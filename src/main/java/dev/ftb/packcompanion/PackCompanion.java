package dev.ftb.packcompanion;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.buffs.MobEntityBuffFeature;
import dev.ftb.packcompanion.features.forcedgamerule.ForcedGameRulesFeature;
import dev.ftb.packcompanion.features.loot.RandomNameLootFeature;
import dev.ftb.packcompanion.features.onboarding.shadernotice.ShaderNotice;
import dev.ftb.packcompanion.features.spawners.SpawnerFeature;
import dev.ftb.packcompanion.features.structures.StructuresFeature;
import dev.ftb.packcompanion.features.triggerblock.TriggerBlockFeature;
import dev.ftb.packcompanion.features.villager.NoWanderingTraderInvisPotions;
import dev.ftb.packcompanion.integrations.Integrations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod(PackCompanionAPI.MOD_ID)
public class PackCompanion {
    public static final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> REGISTRIES = new HashMap<>();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PackCompanionAPI.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final List<BiFunction<IEventBus, ModContainer, Feature>> FEATURES = List.of(
            RandomNameLootFeature::new,
            MobEntityBuffFeature::new,
            SpawnerFeature::new,
            StructuresFeature::new,
            ShaderNotice::new,
            NoWanderingTraderInvisPotions::new,
            TriggerBlockFeature::new,
            ActionPadFeature::new,
            ForcedGameRulesFeature::new
    );

    private final List<Feature> createdFeatures = new ArrayList<>();
    private final PackCompanionDataGen dataGen;

    public PackCompanion() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContainer container = ModLoadingContext.get().getActiveContainer();

        // Set up the features
        FEATURES.forEach(featureConstructor -> {
            Feature feature = featureConstructor.apply(modEventBus, container);
            createdFeatures.add(feature);
        });

        REGISTRIES.forEach((k, e) -> e.register(modEventBus));

        Integrations.instantInit();

        this.dataGen = new PackCompanionDataGen(this);
        modEventBus.addListener(this.dataGen::onInitializeDataGenerator);

        modEventBus.addListener(this::onSetup);
        modEventBus.addListener(this::onClientInit);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverBeforeStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);

        AtomicInteger packetId = new AtomicInteger(0);
        runForFeatures(
                feature -> feature instanceof Feature.Common,
                feature -> ((Feature.Common) feature).registerPackets(NETWORK, packetId)
        );

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
        PCServerConfig.load(event.getServer());
        Integrations.serverInit();
    }

    private void serverStarted(ServerStartedEvent event) {
        runForFeatures(feature -> feature instanceof Feature.Server, feature -> ((Feature.Server) feature).onServerInit(event.getServer()));
    }

    private void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> companionRootCommand = Commands.literal(PackCompanionAPI.MOD_ID);

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

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ServerDataReloader());
    }

    private class ServerDataReloader implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            runForFeatures(
                    feature -> feature instanceof Feature.Common || feature instanceof Feature.Server,
                    feature -> feature.onReload(resourceManager)
            );

            Integrations.onReload(resourceManager);
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

    public List<Feature> features() {
        return createdFeatures;
    }
}
