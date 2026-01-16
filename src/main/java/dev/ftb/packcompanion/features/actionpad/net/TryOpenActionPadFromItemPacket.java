package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.actionpad.PadActions;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.actionpad.ActionPadItem;
import dev.ftb.packcompanion.integrations.InventorySearcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Supplier;

public class TryOpenActionPadFromItemPacket {
    public static final TryOpenActionPadFromItemPacket INSTANCE = new TryOpenActionPadFromItemPacket();

    private TryOpenActionPadFromItemPacket() {}

    public void encode(FriendlyByteBuf buf) {
        // No data to encode
    }

    public static TryOpenActionPadFromItemPacket decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void handle(TryOpenActionPadFromItemPacket ignored, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var player = context.get().getSender();
            if (player == null) {
                return;
            }

            boolean hasPlayersOnline = Optional.ofNullable(player.getServer())
                    .map(e -> e.getPlayerList().getPlayerCount() > 1)
                    .orElse(false);

            if (!FMLEnvironment.production) {
                // Bypass check in dev
                hasPlayersOnline = true;
            }

            for (var itemStack : player.getInventory().items) {
                if (itemStack.getItem() instanceof ActionPadItem) {
                    sendOpenPacket(player, hasPlayersOnline);
                    return;
                }
            }

            // We need to use curios api here as well.
            if (InventorySearcher.INSTANCE.containsItem(player, ActionPadFeature.ACTION_PAD.get())) {
                sendOpenPacket(player, hasPlayersOnline);
            }
        });

        context.get().setPacketHandled(true);
    }

    private static void sendOpenPacket(Player player, boolean playersOnline) {
        PackCompanion.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new OpenActionPadPacket(PadActions.get().getUnlockedActions(player), playersOnline));
    }
}
