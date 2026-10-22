package dev.ftb.packcompanion.mixin.features.structureplacer;

import dev.ftb.packcompanion.features.structureplacer.client.PlacerActionsController;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", ordinal = 2), cancellable = true)
    public void ftbpc$preventKeypressWhilstFocused(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (PlacerActionsController.INSTANCE.isFocused()) {
            if (key == GLFW.GLFW_KEY_W ||
                key == GLFW.GLFW_KEY_S ||
                key == GLFW.GLFW_KEY_A ||
                key == GLFW.GLFW_KEY_D ||
                key == GLFW.GLFW_KEY_Q ||
                key == GLFW.GLFW_KEY_E ||
                key == GLFW.GLFW_KEY_R) {
                ci.cancel();
            }
        }
    }
}
