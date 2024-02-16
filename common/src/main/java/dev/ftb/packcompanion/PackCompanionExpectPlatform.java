package dev.ftb.packcompanion;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackCompanionExpectPlatform {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Function<Screen, Screen> getModListScreen() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasModlistScreen() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IntegrationsEntrypoint getIntegrationEntry() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
}
