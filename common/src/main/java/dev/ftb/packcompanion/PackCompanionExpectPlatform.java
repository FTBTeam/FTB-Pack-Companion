package dev.ftb.packcompanion;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackCompanionExpectPlatform {
    @ExpectPlatform
    public static Path getGameDirectory() {
        return null;
    }

    @ExpectPlatform
    public static Path getConfigDirectory() {
        return null;
    }

    @ExpectPlatform
    public static IntegrationsEntrypoint getIntegrationEntry() {
        return null;
    }
}
