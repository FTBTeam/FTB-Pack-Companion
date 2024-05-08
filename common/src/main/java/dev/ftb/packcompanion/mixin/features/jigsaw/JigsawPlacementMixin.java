package dev.ftb.packcompanion.mixin.features.jigsaw;

import dev.ftb.packcompanion.config.PCCommonConfig;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {
    @ModifyVariable(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;ILnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;)Ljava/util/Optional;", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private static int modifyRange(int range) {
        int r = PCCommonConfig.EXTENDED_JIGSAW_RANGE.get();
        return r == 0 ? range : r;
    }
}
