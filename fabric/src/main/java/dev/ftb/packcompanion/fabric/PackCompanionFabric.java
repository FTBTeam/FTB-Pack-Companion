package dev.ftb.packcompanion.fabric;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.fabric.integrations.IntegrationsFabric;
import net.fabricmc.api.ModInitializer;

public class PackCompanionFabric implements ModInitializer {
    static final IntegrationsFabric integrationsEntry = new IntegrationsFabric();

    @Override
    public void onInitialize() {
        PackCompanion.init();
    }
}
