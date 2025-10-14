package dev.ftb.packcompanion;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.function.Consumer;

public record PackCompanionDataGen(PackCompanion modInstance) {
    public void onInitializeDataGenerator(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        DataGatherCollector collector = new DataGatherCollector();

        for (Feature feature : modInstance.features()) {
            feature.onDataGather(collector);
        }

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new Lang(output, collector));
        generator.addProvider(true, new BlockStateGen(packOutput, existingFileHelper, collector.blockStateProviders()));
        generator.addProvider(true, new ItemModelGen(packOutput, existingFileHelper, collector.itemModelProviders()));
    }

    private static class Lang extends LanguageProvider {
        final DataGatherCollector collector;

        public Lang(PackOutput output, DataGatherCollector collector) {
            super(output, PackCompanion.MOD_ID, "en_us");
            this.collector = collector;
        }

        @Override
        protected void addTranslations() {
            collector.translationCollector().translations().forEach(this::add);
        }
    }

    private static class BlockStateGen extends BlockStateProvider {
        private final List<Consumer<BlockStateProvider>> blockStateProviders;

        public BlockStateGen(PackOutput output, ExistingFileHelper exFileHelper, List<Consumer<BlockStateProvider>> consumers) {
            super(output, PackCompanion.MOD_ID, exFileHelper);
            this.blockStateProviders = consumers;
        }

        @Override
        protected void registerStatesAndModels() {
            for (Consumer<BlockStateProvider> consumer : blockStateProviders) {
                consumer.accept(this);
            }
        }
    }

    private static class ItemModelGen extends ItemModelProvider {
        private final List<Consumer<ItemModelProvider>> itemModelProviders;

        public ItemModelGen(PackOutput output, ExistingFileHelper existingFileHelper, List<Consumer<ItemModelProvider>> consumers) {
            super(output, PackCompanion.MOD_ID, existingFileHelper);
            this.itemModelProviders = consumers;
        }

        @Override
        protected void registerModels() {
            for (Consumer<ItemModelProvider> consumer : itemModelProviders) {
                consumer.accept(this);
            }
        }
    }
}
