package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RunActionPacket(PadAction action) implements CustomPacketPayload {
    public static final Type<RunActionPacket> TYPE = new Type<>(PackCompanion.id("run_action_pad_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RunActionPacket> STREAM_CODEC = StreamCodec.composite(
            PadAction.STREAM_CODEC, RunActionPacket::action,
            RunActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RunActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            packet.action().commandAction()
                    .map(cmd -> (PadAction.ActionRunner) cmd)
                    .or(() -> packet.action().teleportAction().map(tp -> (PadAction.ActionRunner) tp))
                    .ifPresent(actionRunner -> actionRunner.run((ServerPlayer) context.player()));
        });
    }
}
