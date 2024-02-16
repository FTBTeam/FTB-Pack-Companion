package dev.ftb.packcompanion.api.client;

import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackCompanionClientAPI {
    private static boolean initialized = false;
    public static PackCompanionClientAPI INSTANCE = new PackCompanionClientAPI();

    public Map<AdditionalPauseTarget, AdditionalPauseProvider> additionalPauseProviders = new HashMap<>();

    private PackCompanionClientAPI() {
        if (initialized) {
            throw new RuntimeException("FTBPackCompanionClientAPI has already been initialized!");
        }

        initialized = true;
    }

    public static PackCompanionClientAPI get() {
        return INSTANCE;
    }

    public void registerAdditionalPauseProvider(AdditionalPauseTarget target, AdditionalPauseProvider provider) {
        additionalPauseProviders.put(target, provider);
    }
}
