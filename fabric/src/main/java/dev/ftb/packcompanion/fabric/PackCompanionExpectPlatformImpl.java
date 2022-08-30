package dev.ftb.packcompanion.fabric;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
