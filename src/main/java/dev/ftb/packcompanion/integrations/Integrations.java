package dev.ftb.packcompanion.integrations;

import dev.ftb.packcompanion.integrations.fancymenu.FancyMenuIntegration;
import net.neoforged.fml.ModList;

import java.util.function.Supplier;

public class Integrations {
    public static void clientInit() {
        loadIfModPresent("fancymenu", () -> FancyMenuIntegration::init);
    }

    public static void serverInit() {
    }

    public static void commonInit() {
    }

    private static void loadIfModPresent(String modId, Supplier<Runnable> runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.get().run();
        }
    }
}
