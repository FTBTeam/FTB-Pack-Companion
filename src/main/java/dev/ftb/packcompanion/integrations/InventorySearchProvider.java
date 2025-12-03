package dev.ftb.packcompanion.integrations;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public interface InventorySearchProvider {
    boolean containsItem(Player player, Item stack);
}
