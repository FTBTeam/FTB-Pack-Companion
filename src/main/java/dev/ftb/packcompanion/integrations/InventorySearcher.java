package dev.ftb.packcompanion.integrations;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public enum InventorySearcher implements InventorySearchProvider {
    INSTANCE;

    private final List<InventorySearchProvider> providers = new ArrayList<>();

    public void registerProvider(InventorySearchProvider provider) {
        providers.add(provider);
    }

    @Override
    public boolean containsItem(Player player, Item stack) {
        for (InventorySearchProvider provider : providers) {
            if (provider.containsItem(player, stack)) {
                return true;
            }
        }

        return false;
    }
}
