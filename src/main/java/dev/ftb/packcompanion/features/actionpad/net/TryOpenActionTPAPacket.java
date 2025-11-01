package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.utils.NameAndUuid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Objects;

public enum TryOpenActionTPAPacket implements CustomPacketPayload {
    INSTANCE;

    public static final Type<TryOpenActionTPAPacket> TYPE = new Type<>(PackCompanion.id("try_open_action_tpa"));
    public static final StreamCodec<FriendlyByteBuf, TryOpenActionTPAPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TryOpenActionTPAPacket ignored, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            List<NameAndUuid> users = Objects.requireNonNull(context.player().getServer())
                    .getPlayerList()
                    .getPlayers()
                    .stream()
                    .filter(p -> !FMLEnvironment.production || !p.getUUID().equals(player.getUUID()))
                    .map(p -> new NameAndUuid(p.getName().getString(), p.getUUID()))
                    .toList();

            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenTPAPacket(users));
        });
    }
}
