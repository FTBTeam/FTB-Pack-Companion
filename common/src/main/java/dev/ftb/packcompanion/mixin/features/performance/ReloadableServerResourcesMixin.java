package dev.ftb.packcompanion.mixin.features.performance;

import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.21.3")
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

    @Redirect(method = "updateRegistryTags()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"))
    private void rebuildBlockCache() {
        if (!PCServerConfig.RELOAD_PERFORMANCE.get()) {
            Blocks.rebuildCache();
        }
    }
}
