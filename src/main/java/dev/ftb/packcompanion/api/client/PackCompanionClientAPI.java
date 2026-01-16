package dev.ftb.packcompanion.api.client;

import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;

import java.util.*;

public class PackCompanionClientAPI {
    private static boolean initialized = false;
    public static PackCompanionClientAPI INSTANCE = new PackCompanionClientAPI();

    private EnumMap<AdditionalPauseTarget, Set<AdditionalPauseProvider>> additionalPauseProviders = new EnumMap<>(AdditionalPauseTarget.class);

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
        if (!additionalPauseProviders.containsKey(target)) {
            additionalPauseProviders.put(target, new HashSet<>());
        }

        var providers = additionalPauseProviders.get(target);
        providers.add(provider);

        additionalPauseProviders.put(target, providers);
    }

    public EnumMap<AdditionalPauseTarget, Set<AdditionalPauseProvider>> getAdditionalPauseProviders() {
        return additionalPauseProviders;
    }
}
