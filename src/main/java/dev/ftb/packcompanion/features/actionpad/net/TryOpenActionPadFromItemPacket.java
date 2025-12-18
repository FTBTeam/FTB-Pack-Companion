package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.actionpad.PadActions;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.actionpad.ActionPadItem;
import dev.ftb.packcompanion.integrations.InventorySearcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public enum TryOpenActionPadFromItemPacket implements CustomPacketPayload {
    INSTANCE;

    public static final Type<TryOpenActionPadFromItemPacket> TYPE = new Type<>(PackCompanion.id("try_open_action_pad_from_item"));
    public static final StreamCodec<FriendlyByteBuf, TryOpenActionPadFromItemPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TryOpenActionPadFromItemPacket ignored, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
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
    }

    private static void sendOpenPacket(Player player, boolean playersOnline) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenActionPadPacket(PadActions.get().getUnlockedActions((ServerPlayer) player), playersOnline));
    }
}
