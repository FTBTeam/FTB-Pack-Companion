package dev.ftb.packcompanion.features.notoast;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Tutorial.class)
public abstract class TutorialMixin {
    @Shadow @Nullable private TutorialStepInstance instance;

    @Shadow public abstract void stop();

    @Inject(at = @At("HEAD"), method = "start()V", cancellable = true)
    private void start(CallbackInfo info) {
        if (enabled()) {
            if (this.instance != null) {
                this.stop();
            }

            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
    private void tick(CallbackInfo info) {
        if (enabled()) {
            info.cancel();
        }
    }

    /**
     * We have to default to false if the config isn't loaded yet as this can run very early
     *
     * @return if the toast feature should be run
     */
    private static boolean enabled() {
        return PCClientConfig.DISABLE_TUTORIAL_TOASTS.get();
    }
}
