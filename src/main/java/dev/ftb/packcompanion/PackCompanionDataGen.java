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
            add("ftbpackcompanion.pause.mods", "Mods (%s)");
            add("ftbpackcompanion.tooltip.support_discord", "Need help? Join our Discord server!");
            add("ftbpackcompanion.tooltip.support_github", "Found a bug? Report it on GitHub!");
        }
    }
}
