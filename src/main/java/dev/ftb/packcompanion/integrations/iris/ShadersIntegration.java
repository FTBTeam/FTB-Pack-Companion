package dev.ftb.packcompanion.integrations.iris;

import net.neoforged.fml.ModList;

public class ShadersIntegration {
    private static ShadersIntegration instance;

    private ShaderProvider provider;

    public static ShadersIntegration get() {
        if (instance == null) {
            instance = new ShadersIntegration();
        }

        return instance;
    }

    public ShadersIntegration() {
        if (ModList.get().isLoaded("iris")) {
            provider = new IrisProvider();
        }
    }

    public boolean isAvailable() {
        return provider != null;
    }

    public ShaderProvider provider() {
        return provider;
    }
}
