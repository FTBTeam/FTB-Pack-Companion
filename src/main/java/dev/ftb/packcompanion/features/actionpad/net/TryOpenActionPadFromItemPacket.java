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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TryOpenActionPadFromItemPacket() implements CustomPacketPayload {
    public static final Type<TryOpenActionPadFromItemPacket> TYPE = new Type<>(PackCompanion.id("try_open_action_pad_from_item"));
    public static final StreamCodec<FriendlyByteBuf, TryOpenActionPadFromItemPacket> STREAM_CODEC = StreamCodec.unit(new TryOpenActionPadFromItemPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TryOpenActionPadFromItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            for (var itemStack : player.getInventory().items) {
                if (itemStack.getItem() instanceof ActionPadItem) {
                    sendOpenPacket(player);
                    return;
                }
            }

            // We need to use curios api here as well.
            if (InventorySearcher.INSTANCE.containsItem(player, ActionPadFeature.ACTION_PAD.get())) {
                sendOpenPacket(player);
            }
        });
    }

    private static void sendOpenPacket(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenActionPadPacket(PadActions.get().getUnlockedActions(player)));
    }
}
