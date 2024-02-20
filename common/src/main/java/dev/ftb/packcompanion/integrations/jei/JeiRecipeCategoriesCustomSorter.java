package dev.ftb.packcompanion.integrations.jei;

import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.config.PCClientConfig;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class JeiRecipeCategoriesCustomSorter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JeiRecipeCategoriesCustomSorter.class);

    public static final JeiRecipeCategoriesCustomSorter INSTANCE = new JeiRecipeCategoriesCustomSorter();

    private final LinkedList<String> sortingOrder = new LinkedList<>();
    private boolean hasCustomSorting = false;

    public JeiRecipeCategoriesCustomSorter() {
        // We don't run updateSorting here because it's called on level load anyway
    }

    /**
     * Implements the following sorting logic:
     * - If it's minecraft: it's always first regardless of sorting
     * - If {@code sortingOrder} has the category, it's sorted to the top in the order it's in the list
     * - If it's not in the list, it's sorted to the bottom ideally in it's original order
     *
     * @param recipeCategories The list of recipe categories to sort
     * @return The sorted list of recipe categories
     */
    public List<IRecipeCategory<?>> sort(List<IRecipeCategory<?>> recipeCategories) {
        // If we don't have custom sorting, just return the original list
        if (!this.hasCustomSorting) return recipeCategories;

        // Copy the list so we don't modify the original
        LinkedList<IRecipeCategory<?>> sortedCategories = new LinkedList<>(recipeCategories);

        // Now apply sorting logic
        sortedCategories.sort((a, b) -> {
            ResourceLocation aId = a.getRecipeType().getUid();
            ResourceLocation bId = b.getRecipeType().getUid();

            // If it's minecraft, it's always first
            if (aId.getNamespace().equals("minecraft")) return -1;
            if (bId.getNamespace().equals("minecraft")) return 1;

            // If it's in the sorting order, sort it to the top
            int aIndex = sortingOrder.indexOf(aId.toString());
            int bIndex = sortingOrder.indexOf(bId.toString());

            if (aIndex != -1 && bIndex != -1) {
                return Integer.compare(aIndex, bIndex);
            } else if (aIndex != -1) {
                return -1;
            } else if (bIndex != -1) {
                return 1;
            }

            // If it's not in the sorting order, sort it to the bottom
            return Integer.compare(recipeCategories.indexOf(a), recipeCategories.indexOf(b));
        });

        return recipeCategories;
    }

    private boolean loadSortingOrder() {
        var sortingOrderPath = PackCompanionExpectPlatform.getGameDirectory().resolve("data/" + PackCompanionAPI.MOD_ID + "/jei_recipe_category_sorting_order.txt");
        if (Files.notExists(sortingOrderPath)) {
            return false;
        }

        try {
            // Just read in each line and trust that this is a valid sorting order
            List<String> lines = Files.readAllLines(sortingOrderPath);
            this.sortingOrder.addAll(lines);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to load custom JEI recipe category sorting order", e);
        }

        return false;
    }

    public void updateSorting() {
        if (!PCClientConfig.JEI_SORT_RECIPES.get()) {
            LOGGER.info("JEI recipe sorting is disabled, not updating sorting");
            return;
        }

        LOGGER.info("Reloading JEI recipe category sorting order");
        this.sortingOrder.clear();
        this.hasCustomSorting = loadSortingOrder();
        if (this.sortingOrder.isEmpty()) {
            LOGGER.info("No custom JEI recipe category sorting order found within `jei_recipe_category_sorting_order.txt`, using default sorting");
            this.hasCustomSorting = false;
        }
    }
}
