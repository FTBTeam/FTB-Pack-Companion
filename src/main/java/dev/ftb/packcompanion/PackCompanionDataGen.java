package dev.ftb.packcompanion;

import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import top.theillusivec4.curios.api.CuriosDataProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

        BlockTagGen blockTagProvider = new BlockTagGen(packOutput, event.getLookupProvider(), existingFileHelper, collector);
        generator.addProvider(true, blockTagProvider);
        generator.addProvider(true, new ItemTagGen(packOutput, event.getLookupProvider(), blockTagProvider.contentsGetter(), existingFileHelper, collector));

        generator.addProvider(true, new CuriosDataGen(
                packOutput,
                existingFileHelper,
                event.getLookupProvider()
        ));
    }

    private static class Lang extends LanguageProvider {
        final DataGatherCollector collector;

        public Lang(PackOutput output, DataGatherCollector collector) {
            super(output, PackCompanionAPI.MOD_ID, "en_us");
            this.collector = collector;
        }

        @Override
        protected void addTranslations() {
            collector.translationCollector().translations().forEach(this::add);

            add("ftbpackcompanion.pause.mods", "Mods (%s)");
            add("ftbpackcompanion.tooltip.support_discord", "Need help? Join our Discord server!");
            add("ftbpackcompanion.tooltip.support_github", "Found a bug? Report it on GitHub!");
        }
    }

    private static class BlockStateGen extends BlockStateProvider {
        private final List<Consumer<BlockStateProvider>> blockStateProviders;

        public BlockStateGen(PackOutput output, ExistingFileHelper exFileHelper, List<Consumer<BlockStateProvider>> consumers) {
            super(output, PackCompanionAPI.MOD_ID, exFileHelper);
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
            super(output, PackCompanionAPI.MOD_ID, existingFileHelper);
            this.itemModelProviders = consumers;
        }

        @Override
        protected void registerModels() {
            for (Consumer<ItemModelProvider> consumer : itemModelProviders) {
                consumer.accept(this);
            }
        }
    }

    public static class BlockTagGen extends BlockTagsProvider {
        private final List<Consumer<BlockTagGen>> blockStateProviders;

        public BlockTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper, DataGatherCollector collector) {
            super(output, lookupProvider, PackCompanionAPI.MOD_ID, existingFileHelper);
            this.blockStateProviders = collector.blockTagProviders();
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (Consumer<BlockTagGen> consumer : blockStateProviders) {
                consumer.accept(this);
            }
        }

        public IntrinsicTagAppender<Block> appendBlockTag(TagKey<Block> tag) {
            return this.tag(tag);
        }
    }

    public static class ItemTagGen extends ItemTagsProvider {
        private final List<Consumer<ItemTagGen>> itemTagProviders;

        public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper, DataGatherCollector collector) {
            super(output, lookupProvider, blockTags, PackCompanionAPI.MOD_ID, existingFileHelper);
            this.itemTagProviders = collector.itemTagProviders();
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (Consumer<ItemTagGen> consumer : itemTagProviders) {
                consumer.accept(this);
            }
        }

        public IntrinsicTagAppender<Item> appendItemTag(TagKey<Item> tag) {
            return this.tag(tag);
        }
    }

    private static class CuriosDataGen extends CuriosDataProvider {
        public CuriosDataGen(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
            super(PackCompanionAPI.MOD_ID, output, fileHelper, registries);
        }

        @Override
        public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
            this.createEntities("curio")
                    .addPlayer()
                    .addSlots("curio");
        }
    }
}
