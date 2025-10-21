package dev.ftb.packcompanion.features.teleporter;

import dev.ftb.packcompanion.features.teleporter.net.OpenTeleporterPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class TeleporterItem extends Item {
    public TeleporterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenTeleporterPacket(TeleporterDestinations.get().getUnlockedDestinations(player)));
        }

        return super.use(level, player, usedHand);
    }
}
