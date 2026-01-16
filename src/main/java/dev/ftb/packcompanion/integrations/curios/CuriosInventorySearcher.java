package dev.ftb.packcompanion.integrations.curios;

import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.integrations.InventorySearchProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;


public class CuriosInventorySearcher implements InventorySearchProvider {
    @Override
    public boolean containsItem(Player player, Item stack) {
        LazyOptional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);

        return curiosInventory
                .map(handler -> handler.findFirstCurio(ActionPadFeature.ACTION_PAD.get()).isPresent())
                .orElse(false);
    }
}
