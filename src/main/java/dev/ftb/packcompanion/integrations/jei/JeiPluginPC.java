package dev.ftb.packcompanion.integrations.jei;

import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.config.PCClientConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@JeiPlugin
public class JeiPluginPC implements IModPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JeiPluginPC.class);
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(PackCompanionAPI.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (PCClientConfig.JEI_RECIPE_NAMES_DEBUG.get()) {
            jeiRuntime.getRecipeManager().createRecipeCategoryLookup().get().forEach(category -> {
                LOGGER.info("JEI Recipe Category: {} // {}", category.getRecipeType().getUid(), category.getTitle().getString());
            });
        }
    }
}
