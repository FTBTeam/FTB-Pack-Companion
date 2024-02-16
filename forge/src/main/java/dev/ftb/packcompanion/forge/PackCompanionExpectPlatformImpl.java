package dev.ftb.packcompanion.forge;

import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Function;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Function<Screen, Screen> getModListScreen() {
        return ModListScreen::new;
    }

    public static boolean hasModlistScreen() {
        return true;
    }

    public static IntegrationsEntrypoint getIntegrationEntry() {
        return PackCompanionForge.integrationsEntry;
    }
}
