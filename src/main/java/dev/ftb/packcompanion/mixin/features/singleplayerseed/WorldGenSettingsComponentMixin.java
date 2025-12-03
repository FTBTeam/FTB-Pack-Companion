package dev.ftb.packcompanion.mixin.features.singleplayerseed;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class WorldGenSettingsComponentMixin {

    @Shadow @Final WorldCreationUiState uiState;

    @Inject(method = "onCreate", at = @At("HEAD"))
    void onCreateFinal(CallbackInfo ci) {
        if (PCClientConfig.WORLD_USES_STATIC_SEED.get() && !PCClientConfig.STATIC_SEED.get().isEmpty()) {
            this.uiState.setSeed(PCClientConfig.STATIC_SEED.get());
        }
    }
}
