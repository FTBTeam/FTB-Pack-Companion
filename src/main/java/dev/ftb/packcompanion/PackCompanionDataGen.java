package dev.ftb.packcompanion;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public record PackCompanionDataGen(PackCompanion modInstance) {
    public void onInitializeDataGenerator(GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();

        DataGatherCollector collector = new DataGatherCollector();

        for (Feature feature : modInstance.features()) {
            feature.onDataGather(collector);
        }

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new Lang(output, collector));
//        generator.addProvider(true, new BlockStateGen(packOutput, collector.blockStateProviders()));
        generator.addProvider(true, new ModelGenerator(packOutput, collector.itemModelProviders(), collector.blockStateProviders()));

        generator.addProvider(true, new BlockTagGen(packOutput, event.getLookupProvider(), collector));
        generator.addProvider(true, new ItemTagGen(packOutput, event.getLookupProvider(), collector));

        generator.addProvider(true, new CuriosDataGen(
                packOutput,
                event.getLookupProvider()
        ));
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

    private static class ModelGenerator extends ModelProvider {
        private final List<Consumer<ItemModelGenerators>> itemModelProviders;
        private final List<Consumer<BlockModelGenerators>> blockModelProviders;

        public ModelGenerator(PackOutput output, List<Consumer<ItemModelGenerators>> itemModels, List<Consumer<BlockModelGenerators>> blockModels) {
            super(output, PackCompanion.MOD_ID);
            this.itemModelProviders = itemModels;
            this.blockModelProviders = blockModels;
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            for (Consumer<ItemModelGenerators> consumer : itemModelProviders) {
                consumer.accept(itemModels);
            }

            for (Consumer<BlockModelGenerators> consumer : blockModelProviders) {
                consumer.accept(blockModels);
            }
        }
    }

    public static class BlockTagGen extends BlockTagsProvider {
        private final List<Consumer<BlockTagGen>> blockStateProviders;

        public BlockTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, DataGatherCollector collector) {
            super(output, lookupProvider, PackCompanion.MOD_ID);
            this.blockStateProviders = collector.blockTagProviders();
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (Consumer<BlockTagGen> consumer : blockStateProviders) {
                consumer.accept(this);
            }
        }

        public TagAppender<Block, Block> appendBlockTag(TagKey<Block> tag) {
            return this.tag(tag);
        }
    }

    public static class ItemTagGen extends ItemTagsProvider {
        private final List<Consumer<ItemTagGen>> itemTagProviders;

        public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, DataGatherCollector collector) {
            super(output, lookupProvider, PackCompanion.MOD_ID);
            this.itemTagProviders = collector.itemTagProviders();
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (Consumer<ItemTagGen> consumer : itemTagProviders) {
                consumer.accept(this);
            }
        }

        public TagAppender<Item, Item> appendItemTag(TagKey<Item> tag) {
            return this.tag(tag);
        }
    }

    private static class CuriosDataGen extends CuriosDataProvider {
        public CuriosDataGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(PackCompanion.MOD_ID, output, registries);
        }

        @Override
        public void generate(HolderLookup.Provider registries) {
            this.createEntities("curio")
                    .addPlayer()
                    .addSlots("curio");
        }
    }
}
