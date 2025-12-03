package dev.ftb.packcompanion.mixin.features.notoast;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class AdvancementToastMixin {
    /**
     * JOB: Rejects the additions of toasts of types that we want to block
     */
    @Inject(method = "addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V", at = @At("HEAD"), cancellable = true)
    void addToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof RecipeToast && PCClientConfig.DISABLE_RECIPE_TOASTS.get()) {
            ci.cancel();
        }

        if (toast instanceof AdvancementToast && PCClientConfig.DISABLE_ADVANCEMENT_TOASTS.get()) {
            ci.cancel();
        }
    }
}
