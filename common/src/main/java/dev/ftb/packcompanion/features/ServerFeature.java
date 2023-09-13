package dev.ftb.packcompanion.features;

import net.minecraft.server.MinecraftServer;

public abstract class ServerFeature extends CommonFeature {
    private boolean initialized = false;
    private MinecraftServer server;

    public void setup(MinecraftServer server) {
        if (isDisabled()) {
            return;
        }

        this.server = server;

        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    public MinecraftServer getServer() {
        return server;
    }
}
