package dev.ftb.packcompanion.mixin.features.strongholdplacement;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import dev.ftb.packcompanion.config.PCCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGeneratorStructureState.class)
public class ChunkGeneratorStructureStateMixin {
    @Inject(method = "generateRingPositions", at = @At(value = "INVOKE", target = "Ljava/lang/Math;cos(D)D", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void ftb_pack_companion$onGenerateRingPositions(Holder<StructureSet> structureSet, ConcentricRingsStructurePlacement placement, CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> info, @Local(ordinal = 0) int i, @Local(ordinal = 4) int i1, @Local(ordinal = 1) LocalDoubleRef d1Ref) {
        if (PCCommonConfig.REMOVE_CONCENTRIC_RING_PLACEMENT_BIAS.get()) {
            d1Ref.set(4.0 * i + i * i1 * 6.0);
        }
    }
}
