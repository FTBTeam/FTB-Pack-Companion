package dev.ftb.packcompanion.fabric;

import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.function.Function;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Function<Screen, Screen> getModListScreen() {
        return null;
    }

    public static boolean hasModlistScreen() {
        return false;
    }

    public static IntegrationsEntrypoint getIntegrationEntry() {
        return PackCompanionFabric.integrationsEntry;
    }
}
