package dev.ftb.packcompanion.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PackCompanionExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
