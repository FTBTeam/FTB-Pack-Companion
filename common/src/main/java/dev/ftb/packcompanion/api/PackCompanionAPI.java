package dev.ftb.packcompanion.api;

public class PackCompanionAPI {
    public static final String MOD_ID = "ftbpc";

    private static boolean initialized = false;

    public static PackCompanionAPI INSTANCE = new PackCompanionAPI();

    private PackCompanionAPI() {
        if (initialized) {
            throw new RuntimeException("FTBPackCompanionAPI has already been initialized!");
        }

        initialized = true;
    }

    public static PackCompanionAPI get() {
        return INSTANCE;
    }
}
