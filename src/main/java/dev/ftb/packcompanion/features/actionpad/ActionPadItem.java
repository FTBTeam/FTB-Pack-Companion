package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.packcompanion.features.actionpad.net.OpenActionPadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class ActionPadItem extends Item {
    public ActionPadItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            boolean hasPlayersOnline = Optional.ofNullable(player.level().getServer())
                    .map(e -> e.getPlayerList().getPlayerCount() > 1)
                    .orElse(false);

            if (!FMLEnvironment.isProduction()) {
                // Bypass check in dev
                hasPlayersOnline = true;
            }

            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenActionPadPacket(PadActions.getUnlockedActions((ServerPlayer) player), hasPlayersOnline));
        }

        return super.use(level, player, usedHand);
    }
}
