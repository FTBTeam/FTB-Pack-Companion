package dev.ftb.packcompanion.fabric;


import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static IntegrationsEntrypoint getIntegrationEntry() {
        return PackCompanionFabric.integrationsEntry;
    }
}
