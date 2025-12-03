package dev.ftb.packcompanion.mixin.features.pausescreen;

import dev.ftb.packcompanion.features.pausemenuapi.CustomPauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseMenuMixin extends Screen {
    protected PauseMenuMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At("HEAD"), cancellable = true)
    private void ftbpc$createPauseMenu(CallbackInfo ci) {
        if (!CustomPauseScreen.DISABLE_CUSTOM_PAUSE) {
            Minecraft.getInstance().setScreen(new CustomPauseScreen());
            ci.cancel();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        CustomPauseScreen.DISABLE_CUSTOM_PAUSE = false;
    }
}
