package dev.ftb.packcompanion.registry;

import dev.ftb.packcompanion.features.loot.RandomNameLootFunction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public enum ReloadResourceManager implements ResourceManagerReloadListener {
    INSTANCE;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        RandomNameLootFunction.clearCache();
    }
}
