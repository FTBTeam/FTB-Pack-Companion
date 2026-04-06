package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
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

            List<NameAndId> users = Objects.requireNonNull(context.player().level().getServer())
                    .getPlayerList()
                    .getPlayers()
                    .stream()
                    .filter(p -> !FMLEnvironment.isProduction() || !p.getUUID().equals(player.getUUID()))
                    .map(p -> new NameAndId(p.getGameProfile()))
                    .toList();

            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenTPAPacket(users));
        });
    }
}
