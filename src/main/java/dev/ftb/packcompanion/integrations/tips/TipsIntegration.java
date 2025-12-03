package dev.ftb.packcompanion.integrations.tips;

import dev.ftb.packcompanion.features.pausemenuapi.CustomPauseScreen;
import net.darkhax.tipsmod.api.TipsAPI;

public class TipsIntegration {
    public static void init() {
        TipsAPI.registerTipScreen(CustomPauseScreen.class);
    }
}
