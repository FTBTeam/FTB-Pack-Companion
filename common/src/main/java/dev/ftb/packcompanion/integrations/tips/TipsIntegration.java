package dev.ftb.packcompanion.integrations.tips;

import dev.ftb.packcompanion.client.screen.pause.CustomPauseScreen;
import net.darkhax.tipsmod.api.TipsAPI;

public class TipsIntegration {
    public static void init() {
        TipsAPI.registerTipScreen(CustomPauseScreen.class);
    }
}
