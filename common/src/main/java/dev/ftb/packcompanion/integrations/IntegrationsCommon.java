package dev.ftb.packcompanion.integrations;

import dev.architectury.platform.Platform;
import dev.ftb.packcompanion.integrations.minetogether.MineTogetherIntegration;
import dev.ftb.packcompanion.integrations.tips.TipsIntegration;

import java.util.function.Supplier;

public class IntegrationsCommon implements IntegrationsEntrypoint {
    public static void loadIfPresent(String modid, Supplier<Runnable> runnable) {
        if (Platform.isModLoaded(modid)) {
            runnable.get().run();
        }
    }

    @Override
    public void onCommonInit() {

    }

    @Override
    public void onServerInit() {

    }

    @Override
    public void onClientInit() {
        loadIfPresent("tipsmod", () -> TipsIntegration::init);
        loadIfPresent("minetogether", () -> MineTogetherIntegration::init);
    }
}
