package dev.ftb.packcompanion.features.loot;

import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RandomNameLootFeature extends Feature.Common {
    public static DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<RandomNameLootFunction>> RANDOM_NAMED_LOOT_FUNCTION =
            getRegistry(Registries.LOOT_FUNCTION_TYPE).register("random_loot_item_function", () -> new LootItemFunctionType<>(RandomNameLootFunction.CODEC));

    public RandomNameLootFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        RandomNameLootFunction.clearCache();
    }
}
