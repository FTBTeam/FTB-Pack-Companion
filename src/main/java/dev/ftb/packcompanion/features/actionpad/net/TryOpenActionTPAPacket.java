package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.utils.NameAndUuid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class TryOpenActionTPAPacket {
    public static final TryOpenActionTPAPacket INSTANCE = new TryOpenActionTPAPacket();

    public TryOpenActionTPAPacket() {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public static TryOpenActionTPAPacket decode(FriendlyByteBuf buffer) {
        return INSTANCE;
    }

    public static void handle(TryOpenActionTPAPacket ignored, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var player = context.get().getSender();
            if (player == null) {
                return;
            }

            List<NameAndUuid> users = Objects.requireNonNull(player.getServer())
                    .getPlayerList()
                    .getPlayers()
                    .stream()
                    .filter(p -> !FMLEnvironment.production || !p.getUUID().equals(player.getUUID()))
                    .map(p -> new NameAndUuid(p.getName().getString(), p.getUUID()))
                    .toList();

            PackCompanion.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new OpenTPAPacket(users));
        });

        context.get().setPacketHandled(true);
    }
}
