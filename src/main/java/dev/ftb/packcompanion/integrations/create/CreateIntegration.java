package dev.ftb.packcompanion.integrations.create;

import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

public class CreateIntegration {
    public static void init() {
        PackCompanionClientAPI.get().registerAdditionalPauseProvider(
                AdditionalPauseTarget.MENU_LEFT,
                new dev.ftb.packcompanion.forge.integrations.create.CreatePauseProvider()
        );
    }
}
