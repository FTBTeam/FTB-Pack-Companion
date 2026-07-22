package dev.ftb.packcompanion.mixin.features.reciperemover;

import dev.ftb.packcompanion.features.reciperemover.RecipeRemover;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "lambda$prepare$1", at = @At("HEAD"), cancellable = true)
    private static void ftbpc$skipUnwantedRecipes(List recipeHolders, Identifier id, Recipe recipe, CallbackInfo ci) {
        if (RecipeRemover.getRemovedRecipes().contains(id)) {
            ci.cancel(); // force it into the "skip" branch
        }
    }
}
