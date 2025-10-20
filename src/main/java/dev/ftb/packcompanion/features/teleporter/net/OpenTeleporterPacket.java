package dev.ftb.packcompanion.features.teleporter.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.teleporter.TeleporterAction;
import dev.ftb.packcompanion.features.teleporter.client.TeleporterClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenTeleporterPacket(List<TeleporterAction> destinations) implements CustomPacketPayload {
    public static final Type<OpenTeleporterPacket> TYPE = new Type<>(PackCompanion.id("open_teleporter"));

    public static final StreamCodec<FriendlyByteBuf, OpenTeleporterPacket> STREAM_CODEC = StreamCodec.composite(
            TeleporterAction.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenTeleporterPacket::destinations,
            OpenTeleporterPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenTeleporterPacket packet, IPayloadContext payload) {
        payload.enqueueWork(() -> TeleporterClient.openTeleporterScreen(packet.destinations()));
    }
}
