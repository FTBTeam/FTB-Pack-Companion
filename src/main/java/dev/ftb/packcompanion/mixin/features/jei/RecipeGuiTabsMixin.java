package dev.ftb.packcompanion.mixin.features.jei;

import dev.ftb.packcompanion.integrations.jei.JeiRecipeCategoriesCustomSorter;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeGuiTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Please forgive me for I have sinned! Sorry for mixing into your code Mezz! I will try and find some time to open this
 * as a PR but right now, I needed something quick and this seemed like the most harmless way to do it.
 */
@Pseudo // Don't bitch about it missing if the mod is not present
@Mixin(value = RecipeGuiTabs.class, remap = false)
public class RecipeGuiTabsMixin {
    /**
     * Redirects the recipe categories list to a sorting instance to provide custom sorting.
     * When the sorting is not enabled, we simply pass back the input to avoid any weirdness
     *
     * @param instance the original input
     * @return the sorted input or the original input if sorting is disabled
     */
    @Redirect(method = "updateLayout", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/recipes/IRecipeGuiLogic;getRecipeCategories()Ljava/util/List;"))
    public List<IRecipeCategory<?>> ftbpc$addSortingToJeiTabs(IRecipeGuiLogic instance) {
        return JeiRecipeCategoriesCustomSorter.INSTANCE.sort(instance.getRecipeCategories());
    }
}
