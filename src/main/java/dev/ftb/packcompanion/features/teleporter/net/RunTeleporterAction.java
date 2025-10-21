package dev.ftb.packcompanion.features.teleporter.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.teleporter.TeleporterAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RunTeleporterAction(TeleporterAction destination) implements CustomPacketPayload {
    public static final Type<RunTeleporterAction> TYPE = new Type<>(PackCompanion.id("run_teleporter_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RunTeleporterAction> STREAM_CODEC = StreamCodec.composite(
            TeleporterAction.STREAM_CODEC, RunTeleporterAction::destination,
            RunTeleporterAction::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RunTeleporterAction packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            packet.destination().commandAction()
                    .map(cmd -> (TeleporterAction.ActionRunner) cmd)
                    .or(() -> packet.destination().teleportAction().map(tp -> (TeleporterAction.ActionRunner) tp))
                    .ifPresent(actionRunner -> actionRunner.run((ServerPlayer) context.player()));
        });
    }
}
