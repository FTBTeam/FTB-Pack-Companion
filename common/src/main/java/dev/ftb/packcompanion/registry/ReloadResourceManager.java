package dev.ftb.packcompanion.registry;

import dev.architectury.platform.Platform;
import dev.ftb.packcompanion.features.loot.RandomNameLootFunction;
//import dev.ftb.packcompanion.integrations.jei.JeiIntegration;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public enum ReloadResourceManager implements ResourceManagerReloadListener {
    INSTANCE;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        RandomNameLootFunction.clearCache();
//        if (Platform.isModLoaded("jei")) {
//            JeiIntegration.updateCategories();
//        }
    }
}
