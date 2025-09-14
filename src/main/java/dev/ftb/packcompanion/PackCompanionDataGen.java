package dev.ftb.packcompanion;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = PackCompanion.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PackCompanionDataGen  {
    @SubscribeEvent
    public static void onInitializeDataGenerator(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new Lang(output));
    }

    private static class Lang extends LanguageProvider {

        public Lang(PackOutput output) {
            super(output, PackCompanion.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            prefixed("pause.mods", "Mods (%s)");
            prefixed("tooltip.support_discord", "Need help? Join our Discord server!");
            prefixed("tooltip.support_github", "Found a bug? Report it on GitHub!");

            prefixed("shaders_notice.title", "Would you like to use shaders?");
            prefixed("shaders_notice.no_shaders.title", "No shaders");
            prefixed("shaders_notice.shaders.title", "Shaders");
            prefixed("shaders_notice.no_shaders.description", "Shaders have been included in this pack but are disabled by default. Would you like to enable them? Shaders can be performance-intensive, cause issues with certain mods, and may not be compatible with your hardware.");
            prefixed("shaders_notice.shaders.description", "If you'd like shaders, you can enable them here to experience the pack with a vibrant look and feel. If you encounter any issues, you can always disable them later in the video settings.");
            prefixed("shaders_notice.shaders_btn.disable", "Disable shaders");
            prefixed("shaders_notice.shaders_btn.enable", "Enable shaders");
        }

        private void prefixed(String key, String value) {
            add("ftbpackcompanion." + key, value);
        }
    }
}
