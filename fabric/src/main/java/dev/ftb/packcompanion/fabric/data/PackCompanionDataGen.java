package dev.ftb.packcompanion.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class PackCompanionDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGen) {
        dataGen.addProvider(new Lang(dataGen));
    }

    private static class Lang extends FabricLanguageProvider {

        protected Lang(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
            translationBuilder.add("ftbpackcompanion.pause.mods", "Mods (%s)");
            translationBuilder.add("ftbpackcompanion.tooltip.support_discord", "Need help? Join our Discord server!");
            translationBuilder.add("ftbpackcompanion.tooltip.support_github", "Found a bug? Report it on GitHub!");
        }
    }
}
