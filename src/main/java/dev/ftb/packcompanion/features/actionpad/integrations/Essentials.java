package dev.ftb.packcompanion.features.actionpad.integrations;

import dev.ftb.mods.ftbessentials.config.FTBEConfig;

public class Essentials {
    public static boolean isTPAEnabled() {
        return FTBEConfig.TPA.enabled.get();
    }
}
