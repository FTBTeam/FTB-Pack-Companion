package dev.ftb.packcompanion.mixin.features.spawners;

import dev.ftb.packcompanion.config.PCCommonConfig;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpawnData.class)
public class SpawnDataMixin {
    @Shadow
    @Final
    private Optional<SpawnData.CustomSpawnRules> customSpawnRules;

    @Inject(method = "getCustomSpawnRules", at = @At("RETURN"), cancellable = true)
    public void onGet(CallbackInfoReturnable<Optional<SpawnData.CustomSpawnRules>> cir) {
        if (PCCommonConfig.IGNORE_LIGHT_LEVEL_FOR_SPAWNERS.get()) {
            if (customSpawnRules.isEmpty()) {
                InclusiveRange<Integer> inclusiveRangeDataResult = new InclusiveRange<>(0, 15);
                cir.setReturnValue(Optional.of(new SpawnData.CustomSpawnRules(inclusiveRangeDataResult, inclusiveRangeDataResult)));
            }
        }
    }
}
