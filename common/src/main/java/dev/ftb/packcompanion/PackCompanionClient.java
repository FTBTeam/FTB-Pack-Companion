package dev.ftb.packcompanion;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.Minecraft;

public class PackCompanionClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(PackCompanionClient::clientSetup);
    }

    public static void clientSetup(Minecraft ignored) {
        PCClientConfig.load();
    }
}
