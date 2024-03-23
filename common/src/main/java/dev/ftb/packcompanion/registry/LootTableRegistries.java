package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.features.loot.RandomNameLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public interface LootTableRegistries {
    DeferredRegister<LootItemFunctionType> REGISTRY = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    RegistrySupplier<LootItemFunctionType> RANDOM_NAME_LOOT_FUNCTION = REGISTRY.register("random_loot_item_function", () -> new LootItemFunctionType(new RandomNameLootFunction.Serializer()));
}
