package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.loot.RandomNameLootFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public interface LootTableRegistries {
    DeferredRegister<LootItemFunctionType> REGISTRY = DeferredRegister.create(PackCompanion.MOD_ID, Registry.LOOT_FUNCTION_REGISTRY);

    RegistrySupplier<LootItemFunctionType> RANDOM_NAME_LOOT_FUNCTION = REGISTRY.register("random_loot_item_function", () -> new LootItemFunctionType(new RandomNameLootFunction.Serializer()));
}
