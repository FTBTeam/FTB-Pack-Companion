package dev.ftb.packcompanion;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.client.screen.pause.providers.SupportPauseProvider;
import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.Minecraft;

public class PackCompanionClient {
    public static void init() {
        PackCompanion.commonIntegrationsEntry.onClientInit();
        PackCompanionExpectPlatform.getIntegrationEntry().onClientInit();

        ClientLifecycleEvent.CLIENT_SETUP.register(PackCompanionClient::clientSetup);

        PackCompanionClientAPI.get().registerAdditionalPauseProvider(
                AdditionalPauseTarget.TOP_LEFT,
                new SupportPauseProvider()
        );
    }

    public static void clientSetup(Minecraft ignored) {
        PCClientConfig.load();
    }
}
