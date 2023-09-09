package dev.ftb.packcompanion.mixin.features.jigsaw;

import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.features.jigsaw.CustomJigsawPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {
    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/pieces/PieceGeneratorSupplier$Context;Lnet/minecraft/world/level/levelgen/structure/pools/JigsawPlacement$PieceFactory;Lnet/minecraft/core/BlockPos;ZZ)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private static void onAddPieces(PieceGeneratorSupplier.Context<JigsawConfiguration> ctx, JigsawPlacement.PieceFactory factory, BlockPos startPos, boolean expansionHack, boolean projectStartToHeightmap, CallbackInfoReturnable<Optional<PieceGenerator<JigsawConfiguration>>> cir) {
        int customRange = PCCommonConfig.EXTENDED_JIGSAW_RANGE.get();
        if (customRange != 0) {
            cir.setReturnValue(CustomJigsawPlacement.addPieces(ctx, factory, startPos, expansionHack, projectStartToHeightmap, customRange));
        }
    }
}
