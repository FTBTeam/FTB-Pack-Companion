package dev.ftb.packcompanion.features.teleporter.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.teleporter.TeleporterDestinations;
import dev.ftb.packcompanion.features.teleporter.TeleporterFeature;
import dev.ftb.packcompanion.features.teleporter.TeleporterItem;
import dev.ftb.packcompanion.integrations.InventorySearcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TryOpenTeleporterFromItemPacket() implements CustomPacketPayload {
    public static final Type<TryOpenTeleporterFromItemPacket> TYPE = new Type<>(PackCompanion.id("try_open_teleporter_from_item"));
    public static final StreamCodec<FriendlyByteBuf, TryOpenTeleporterFromItemPacket> STREAM_CODEC = StreamCodec.unit(new TryOpenTeleporterFromItemPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TryOpenTeleporterFromItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            for (var itemStack : player.getInventory().items) {
                if (itemStack.getItem() instanceof TeleporterItem) {
                    sendOpenPacket(player);
                    return;
                }
            }

            // We need to use curios api here as well.
            if (InventorySearcher.INSTANCE.containsItem(player, TeleporterFeature.TELEPORTER_ITEM.get())) {
                sendOpenPacket(player);
            }
        });
    }

    private static void sendOpenPacket(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenTeleporterPacket(TeleporterDestinations.get().getUnlockedDestinations(player)));
    }
}
