package dev.ftb.packcompanion.neoforge;

import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ModListScreen;

import java.nio.file.Path;
import java.util.function.Function;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
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
