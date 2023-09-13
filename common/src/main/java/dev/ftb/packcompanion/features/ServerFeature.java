package dev.ftb.packcompanion.features;

import net.minecraft.server.MinecraftServer;

public abstract class ServerFeature {
    private boolean initialized = false;
    private MinecraftServer server;

    public void setup(MinecraftServer server) {
        if (!isEnabled()) {
            return;
        }

        this.server = server;

        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    public abstract void initialize();

    public MinecraftServer getServer() {
        return server;
    }

    public boolean isEnabled() {
        return false;
    }
}
