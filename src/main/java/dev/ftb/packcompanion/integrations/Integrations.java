package dev.ftb.packcompanion.integrations;

import dev.ftb.packcompanion.integrations.curios.CuriosIntegration;
import net.neoforged.fml.ModList;

import java.util.function.Supplier;

public class Integrations {
    public static void instantInit() {
    }

    public static void clientInit() {
//        loadIfModPresent("fancymenu", () -> FancyMenuIntegration::init);
    }

    public static void serverInit() {
    }

    public static void commonInit() {
        loadIfModPresent("curios", () -> CuriosIntegration::init);
    }

    private static void loadIfModPresent(String modId, Supplier<Runnable> runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.get().run();
        }
    }
}
