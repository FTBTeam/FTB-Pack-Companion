package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.core.utils.NameAndUuid;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenTPAPacket {
    List<NameAndUuid> users;

    public OpenTPAPacket(List<NameAndUuid> users) {
        this.users = users;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(users.size());
        for (NameAndUuid u : users) {
            u.toBuffer(friendlyByteBuf);
        }
    }

    public static OpenTPAPacket decode(FriendlyByteBuf friendlyByteBuf) {
        var size = friendlyByteBuf.readVarInt();
        var list = new ArrayList<NameAndUuid>(size);
        for (int i = 0; i < size; i++) {
            list.add(NameAndUuid.fromBuffer(friendlyByteBuf));
        }

        return new OpenTPAPacket(list);
    }

    public static void handle(OpenTPAPacket packet, Supplier<NetworkEvent.Context> content) {
        content.get().enqueueWork(() -> ActionPadClient.openActionPadTpaScreen(packet.users));
        content.get().setPacketHandled(true);
    }
}
