package dev.ftb.packcompanion;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class PackCompanionDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGen) {
        FabricDataGenerator.Pack pack = dataGen.createPack();
        pack.addProvider(Lang::new);
    }

    private static class Lang extends FabricLanguageProvider {

        protected Lang(FabricDataOutput dataGenerator) {
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
