package dev.ftb.packcompanion.mixin.features.spawners;

import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.features.spawners.SpawnerBehaviourModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(BaseSpawner.class)
public class SpawnerMixin {
    @Unique
    public SpawnerBehaviourModifier ftb_pack_companion$modifier = new SpawnerBehaviourModifier((BaseSpawner) (Object) this);

    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnDelay:I", ordinal = 0))
    private void ftb_pack_companion$onServerTick(ServerLevel serverLevel, BlockPos blockPos, CallbackInfo ci) {
        if (!PCCommonConfig.BREAK_LIGHT_SOURCES_NEAR_SPAWNERS.get()) {
            return;
        }

        this.ftb_pack_companion$modifier.onTick(serverLevel, blockPos);
    }
}
