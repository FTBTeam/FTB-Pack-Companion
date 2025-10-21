package dev.ftb.packcompanion.core;

import dev.ftb.packcompanion.PackCompanionDataGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataGatherCollector {
    private final TranslationCollector translationCollector = new TranslationCollector();

    private final List<Consumer<BlockStateProvider>> blockStateProviders = new ArrayList<>();
    private final List<Consumer<ItemModelProvider>> itemModelProviders = new ArrayList<>();
    private final List<Consumer<PackCompanionDataGen.ItemTagGen>> itemTagProviders = new ArrayList<>();
    private final List<Consumer<PackCompanionDataGen.BlockTagGen>> blockTagProviders = new ArrayList<>();

    public TranslationCollector translationCollector() {
        return translationCollector;
    }

    public List<Consumer<BlockStateProvider>> blockStateProviders() {
        return blockStateProviders;
    }

    public List<Consumer<ItemModelProvider>> itemModelProviders() {
        return itemModelProviders;
    }

    public List<Consumer<PackCompanionDataGen.ItemTagGen>> itemTagProviders() {
        return itemTagProviders;
    }

    public List<Consumer<PackCompanionDataGen.BlockTagGen>> blockTagProviders() {
        return blockTagProviders;
    }

    public void addBlockStateProvider(Consumer<BlockStateProvider> provider) {
        blockStateProviders.add(provider);
    }

    public void addItemModelProvider(Consumer<ItemModelProvider> provider) {
        itemModelProviders.add(provider);
    }

    public void addItemTagProvider(Consumer<PackCompanionDataGen.ItemTagGen> provider) {
        itemTagProviders.add(provider);
    }

    public void addBlockTagProvider(Consumer<PackCompanionDataGen.BlockTagGen> provider) {
        blockTagProviders.add(provider);
    }

    public static class TranslationCollector {
        private final Map<String, String> translations = new HashMap<>();

        public void add(String key, String value) {
            translations.put(key, value);
        }

        public void addBlock(DeferredHolder<Block, ? extends Block> blockHolder, String name) {
            ResourceLocation resourceKey = blockHolder.getKey().location();
            String key = "block." + resourceKey.getNamespace() + "." + resourceKey.getPath();
            add(key, name);
        }

        public void addItem(DeferredHolder<Item, ? extends Item> item, String value) {
            ResourceLocation resourceKey = item.getKey().location();
            String key = "item." + resourceKey.getNamespace() + "." + resourceKey.getPath();
            add(key, value);
        }

        public void prefixed(String key, String value) {
            this.add("ftbpackcompanion." + key, value);
        }

        public Map<String, String> translations() {
            return translations;
        }
    }
}
