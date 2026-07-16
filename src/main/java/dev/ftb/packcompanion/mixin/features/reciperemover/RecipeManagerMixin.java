package dev.ftb.packcompanion.mixin.features.reciperemover;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.packcompanion.features.reciperemover.RecipeRemover;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @ModifyExpressionValue(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"
            )
    )
    private boolean ftbmaterials$skipUnwantedRecipes(boolean original, @Local(name = "resourcelocation") ResourceLocation resourcelocation) {
        if (original) {
            return true;
        }

        if (RecipeRemover.getRemovedRecipes().contains(resourcelocation)) {
            return true; // force it into the "skip" branch
        }

        return original;
    }
}
