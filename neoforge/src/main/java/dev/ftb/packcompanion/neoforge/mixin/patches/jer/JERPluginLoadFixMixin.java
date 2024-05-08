package dev.ftb.packcompanion.neoforge.mixin.patches.jer;

import dev.ftb.packcompanion.neoforge.integrations.jer.JERIntegration;
import jeresources.api.IJERAPI;
import jeresources.neoforge.NeoForgePlatformHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = NeoForgePlatformHelper.class, remap = false)
public class JERPluginLoadFixMixin {
    @Inject(method = "injectApi(Ljeresources/api/IJERAPI;)V", at = @At("HEAD"), cancellable = true)
    private void ftb_pack_companion$inInject(IJERAPI instance, CallbackInfo ci) {
        JERIntegration.loadJerPlugins(instance);
        ci.cancel();
    }
}
