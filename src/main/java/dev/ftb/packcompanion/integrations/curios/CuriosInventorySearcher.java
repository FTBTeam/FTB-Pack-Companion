package dev.ftb.packcompanion.integrations.curios;

import dev.ftb.packcompanion.features.teleporter.TeleporterFeature;
import dev.ftb.packcompanion.integrations.InventorySearchProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosInventorySearcher implements InventorySearchProvider {
    @Override
    public boolean containsItem(Player player, Item stack) {
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);

        return curiosInventory
                .map(handler -> handler.findFirstCurio(TeleporterFeature.TELEPORTER_ITEM.get()).isPresent())
                .orElse(false);
    }
}
