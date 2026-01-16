package dev.ftb.packcompanion.integrations.jei;

public class JeiIntegration {
    public static void init() {
    }

    public static void updateCategories() {
        JeiRecipeCategoriesCustomSorter.INSTANCE.updateSorting();
    }
}
