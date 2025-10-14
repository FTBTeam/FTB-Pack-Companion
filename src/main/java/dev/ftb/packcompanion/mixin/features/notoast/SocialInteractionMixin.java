package dev.ftb.packcompanion.mixin.features.notoast;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class SocialInteractionMixin {

    @Shadow
    @Final
    public Options options;

    @Shadow
    protected abstract boolean isMultiplayerServer();

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isMultiplayerServer()Z"))
    private boolean tick(Minecraft instance) {
        if (!this.options.joinedFirstServer && PCClientConfig.DISABLE_SOCIALINTERACTION_TOASTS.get()){
            this.options.joinedFirstServer = true;
            this.options.save();
        }
        return this.isMultiplayerServer() && !PCClientConfig.DISABLE_SOCIALINTERACTION_TOASTS.get();
    }
}
