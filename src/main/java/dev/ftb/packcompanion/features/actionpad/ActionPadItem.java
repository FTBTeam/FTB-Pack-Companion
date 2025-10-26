package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.packcompanion.features.actionpad.net.OpenActionPadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ActionPadItem extends Item {
    public ActionPadItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenActionPadPacket(PadActions.get().getUnlockedActions(player)));
        }

        return super.use(level, player, usedHand);
    }
}
