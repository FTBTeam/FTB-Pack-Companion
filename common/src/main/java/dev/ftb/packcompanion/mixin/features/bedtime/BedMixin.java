package dev.ftb.packcompanion.mixin.features.bedtime;

import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedMixin {
    @Inject(method = "canSetSpawn", at = @At(value = "HEAD"), cancellable = true)
    private static void canSetSpawnOverride(Level level, CallbackInfoReturnable<Boolean> callback) {
        if (PCCommonConfig.ALLOW_BEDS_IN_THE_NETHER.get()) {
            callback.setReturnValue(true);
        }
    }
}
