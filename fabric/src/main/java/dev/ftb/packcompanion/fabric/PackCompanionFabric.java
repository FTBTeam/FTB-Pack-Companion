package dev.ftb.packcompanion.fabric;

import dev.ftb.packcompanion.PackCompanion;
import net.fabricmc.api.ModInitializer;

public class PackCompanionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PackCompanion.init();
    }
}
