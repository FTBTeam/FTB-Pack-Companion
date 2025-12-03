package dev.ftb.packcompanion.integrations.minetogether;

import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

public class MineTogetherIntegration {
    public static void init() {
        PackCompanionClientAPI.get().registerAdditionalPauseProvider(
                AdditionalPauseTarget.TOP_RIGHT,
                new MineTogetherPauseProvider()
        );
    }
}
