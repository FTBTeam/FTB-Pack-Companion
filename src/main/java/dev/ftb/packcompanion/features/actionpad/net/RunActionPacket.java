package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.actionpad.PadAction.ActionRunner;
import dev.ftb.packcompanion.features.actionpad.PadActions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RunActionPacket(String actionName) implements CustomPacketPayload {
    public static final Type<RunActionPacket> TYPE = new Type<>(PackCompanion.id("run_action_pad_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RunActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RunActionPacket::actionName,
            RunActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RunActionPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            context.enqueueWork(() ->
                    PadActions.get().getAction(serverPlayer, packet.actionName)
                            .flatMap(action -> action.commandAction().map(ActionRunner::asActionRunner)
                                    .or(() -> action.teleportAction().map(ActionRunner::asActionRunner))
                            )
                            .ifPresent(actionRunner -> actionRunner.run(serverPlayer)));
        }
    }
}
