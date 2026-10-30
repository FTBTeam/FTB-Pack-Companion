package dev.ftb.packcompanion.mixin.features.singleplayerseed;

import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenSettingsComponent.class)
public class WorldGenSettingsComponentMixin {
    @Shadow private EditBox seedEdit;

    @Inject(method = "makeSettings", at = @At("HEAD"))
    void onCreateFinal(boolean bl, CallbackInfoReturnable<WorldGenSettings> cir) {
        if (PCClientConfig.WORLD_USES_STATIC_SEED.get() && !PCClientConfig.STATIC_SEED.get().isEmpty()) {
            this.seedEdit.setValue(PCClientConfig.STATIC_SEED.get());
        }
    }
}
