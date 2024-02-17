package dev.ftb.packcompanion.fabric;

import dev.architectury.platform.Platform;
import dev.ftb.packcompanion.fabric.integrations.ModMenuIntegration;
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
        return ModMenuIntegration::createModListScreen;
    }

    public static boolean hasModlistScreen() {
        return Platform.isModLoaded("modmenu");
    }

    public static IntegrationsEntrypoint getIntegrationEntry() {
        return PackCompanionFabric.integrationsEntry;
    }
}
