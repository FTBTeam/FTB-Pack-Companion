package dev.ftb.packcompanion.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataGatherCollector {
    private final TranslationCollector translationCollector = new TranslationCollector();

    private final List<Consumer<BlockStateProvider>> blockStateProviders = new ArrayList<>();
    private final List<Consumer<ItemModelProvider>> itemModelProviders = new ArrayList<>();

    public TranslationCollector translationCollector() {
        return translationCollector;
    }

    public List<Consumer<BlockStateProvider>> blockStateProviders() {
        return blockStateProviders;
    }

    public List<Consumer<ItemModelProvider>> itemModelProviders() {
        return itemModelProviders;
    }

    public void addBlockStateProvider(Consumer<BlockStateProvider> provider) {
        blockStateProviders.add(provider);
    }

    public void addItemModelProvider(Consumer<ItemModelProvider> provider) {
        itemModelProviders.add(provider);
    }

    public static class TranslationCollector {
        private Map<String, String> translations = new HashMap<>();

        public void add(String key, String value) {
            translations.put(key, value);
        }

        public void addBlock(Supplier<Block> blockSupplier, String name) {
            Block block = blockSupplier.get();
            if (block != null) {
                ResourceLocation resourceKey = block.builtInRegistryHolder().getKey().location();
                String key = "block." + resourceKey.getNamespace() + "." + resourceKey.getPath();
                add(key, name);
            }
        }

        public void prefixed(String key, String value) {
            this.add("ftbpackcompanion." + key, value);
        }

        public Map<String, String> translations() {
            return translations;
        }
    }
}
