package dev.ftb.packcompanion.features.raiders;

import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class NoRaiderRailgunFeature extends Feature.Server {
    private static final String IE_MOD_ID = "immersiveengineering";
    private static final ResourceLocation RAILGUN_ID = ResourceLocation.fromNamespaceAndPath(IE_MOD_ID, "railgun");

    public NoRaiderRailgunFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (!ModList.get().isLoaded(IE_MOD_ID)) {
            return;
        }

        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!PCServerConfig.BLOCK_RAIDER_RAILGUNS.get()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }

        if (!(event.getEntity() instanceof Pillager pillager)) {
            return;
        }

        replaceIfRailgun(pillager, EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        replaceIfRailgun(pillager, EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private static void replaceIfRailgun(Pillager pillager, EquipmentSlot slot, ItemStack replacement) {
        ItemStack current = pillager.getItemBySlot(slot);
        if (current.isEmpty()) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(current.getItem());
        if (RAILGUN_ID.equals(id)) {
            pillager.setItemSlot(slot, replacement);
        }
    }
}
