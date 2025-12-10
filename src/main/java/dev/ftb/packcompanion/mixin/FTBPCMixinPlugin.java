package dev.ftb.packcompanion.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin for FTB Pack Companion.
 * Conditionally applies JER mixin only when JustEnoughResources is present.
 */
public class FTBPCMixinPlugin implements IMixinConfigPlugin {

    private static final String JER_BIOME_HELPER_MIXIN = "dev.ftb.packcompanion.mixin.features.jerfix.BiomeHelperMixin";
    private boolean jerLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        // Check if JER is present by looking for the class file without loading it
        jerLoaded = getClass().getClassLoader().getResource("jeresources/api/util/BiomeHelper.class") != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals(JER_BIOME_HELPER_MIXIN)) {
            return jerLoaded;
        }
        return true;
    }

    // Required interface methods - not used
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
