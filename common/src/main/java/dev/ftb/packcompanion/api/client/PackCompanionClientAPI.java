package dev.ftb.packcompanion.api.client;

public class PackCompanionClientAPI {
    private static boolean initialized = false;
    public static PackCompanionClientAPI INSTANCE = new PackCompanionClientAPI();

    private PackCompanionClientAPI() {
        if (initialized) {
            throw new RuntimeException("FTBPackCompanionClientAPI has already been initialized!");
        }

        initialized = true;
    }

    public static PackCompanionClientAPI get() {
        return INSTANCE;
    }
}
