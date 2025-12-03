package dev.ftb.packcompanion.features.loot;

import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.registries.RegistryObject;

public class RandomNameLootFeature extends Feature.Common {
    public static RegistryObject<LootItemFunctionType> RANDOM_NAMED_LOOT_FUNCTION = getRegistry(Registries.LOOT_FUNCTION_TYPE)
                    .register("random_loot_item_function", () -> new LootItemFunctionType(new RandomNameLootFunction.Serializer()));

    public RandomNameLootFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        RandomNameLootFunction.clearCache();
    }
}
