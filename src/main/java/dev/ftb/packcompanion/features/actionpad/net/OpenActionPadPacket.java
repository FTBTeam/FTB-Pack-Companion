package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenActionPadPacket(List<PadAction> actions, boolean playersOnline) implements CustomPacketPayload {
    public static final Type<OpenActionPadPacket> TYPE = new Type<>(PackCompanion.id("open_action_pad"));

    public static final StreamCodec<FriendlyByteBuf, OpenActionPadPacket> STREAM_CODEC = StreamCodec.composite(
            PadAction.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenActionPadPacket::actions,
            ByteBufCodecs.BOOL, OpenActionPadPacket::playersOnline,
            OpenActionPadPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenActionPadPacket packet, IPayloadContext payload) {
        payload.enqueueWork(() -> ActionPadClient.openActionPadScreen(packet.actions(), packet.playersOnline()));
    }
}
