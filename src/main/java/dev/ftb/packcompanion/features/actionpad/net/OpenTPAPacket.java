package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.utils.NameAndUuid;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenTPAPacket(List<NameAndUuid> users) implements CustomPacketPayload {
    public static final Type<OpenTPAPacket> TYPE = new Type<>(PackCompanion.id("open_tpa"));

    public static final StreamCodec<FriendlyByteBuf, OpenTPAPacket> STREAM_CODEC = StreamCodec.composite(
            NameAndUuid.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenTPAPacket::users,
            OpenTPAPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenTPAPacket packet, IPayloadContext payload) {
        payload.enqueueWork(() -> ActionPadClient.openActionPadTpaScreen(packet.users()));
    }
}
