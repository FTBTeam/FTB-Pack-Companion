package dev.ftb.packcompanion.mixin.features.performance;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.multiplayer.TagCollector;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.21.3")
@Mixin(TagCollector.class)
public class TagCollectorMixin {

    @Inject(method = "refreshBuiltInTagDependentData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"), cancellable = true)
    private static void rebuildBlockCache(CallbackInfo ci) {
        if (PCClientConfig.RELOAD_PERFORMANCE.get()) {
            ci.cancel();
        }
    }
}