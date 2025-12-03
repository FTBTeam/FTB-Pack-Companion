package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ItemsRegistries {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registries.ITEM);

    static {
        ITEMS.register("trigger_block", () -> new BlockItem(BlocksRegistries.TRIGGER_BLOCK.get(), new Item.Properties()));
    }
}
