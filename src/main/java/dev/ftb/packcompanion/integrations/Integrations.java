package dev.ftb.packcompanion.integrations;

import dev.ftb.packcompanion.integrations.create.CreateIntegration;
import dev.ftb.packcompanion.integrations.jei.JeiIntegration;
import dev.ftb.packcompanion.integrations.minetogether.MineTogetherIntegration;
import dev.ftb.packcompanion.integrations.tips.TipsIntegration;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

public class Integrations {
    public static void instantInit() {
    }

    public static void clientInit() {
        loadIfModPresent("tipsmod", () -> TipsIntegration::init);
        loadIfModPresent("minetogether", () -> MineTogetherIntegration::init);
    }

    public static void serverInit() {
    }

    public static void commonInit() {
        loadIfModPresent("create", () -> CreateIntegration::init);
    }

    public static void onReload(ResourceManager manager) {
        if (ModList.get().isLoaded("jei")) {
            JeiIntegration.updateCategories();
        }
    }

    private static void loadIfModPresent(String modId, Supplier<Runnable> runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.get().run();
        }
    }
}
