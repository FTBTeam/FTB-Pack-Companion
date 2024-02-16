package dev.ftb.packcompanion.forge.integrations.create;

import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

public class CreateIntegration {
    public static void init() {
        PackCompanionClientAPI.get().registerAdditionalPauseProvider(
                AdditionalPauseTarget.MENU_LEFT,
                new CreatePauseProvider()
        );
    }
}
