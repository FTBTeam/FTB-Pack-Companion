package dev.ftb.packcompanion;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.features.CommonFeature;
import dev.ftb.packcompanion.features.Features;
import dev.ftb.packcompanion.features.ServerFeature;
import dev.ftb.packcompanion.features.spawners.SpawnerManager;
import dev.ftb.packcompanion.registry.LootTableRegistries;
import dev.ftb.packcompanion.registry.ReloadResourceManager;
import dev.ftb.packcompanion.registry.StructureProcessorRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;

public class PackCompanion {
    public static final String MOD_ID = "ftbpc";

    public static void init() {
        // Registry
        LootTableRegistries.REGISTRY.register();
        StructureProcessorRegistry.REGISTRY.register();

        // Reload listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA, ReloadResourceManager.INSTANCE);

        CommandRegistrationEvent.EVENT.register(CommandRegistry::setup);

        LifecycleEvent.SERVER_BEFORE_START.register(PackCompanion::serverBeforeStart);
        LifecycleEvent.SERVER_STARTED.register(PackCompanion::serverStarted);
        LifecycleEvent.SETUP.register(PackCompanion::onSetup);

        EnvExecutor.runInEnv(Env.CLIENT, () -> PackCompanionClient::init);
    }

    private static void onSetup() {
        PCCommonConfig.load();
        Features.INSTANCE.getCommonFeatures().forEach(CommonFeature::setup);
    }

    private static void serverBeforeStart(MinecraftServer server) {
        PCServerConfig.load(server);
    }

    private static void serverStarted(MinecraftServer server) {
        Features.INSTANCE.getServerFeatures().forEach(e -> e.setup(server));
    }
}
