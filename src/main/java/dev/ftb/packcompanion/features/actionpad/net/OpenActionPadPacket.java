package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenActionPadPacket {
    List<PadAction> actions;
    boolean playersOnline;

    public OpenActionPadPacket(List<PadAction> actions, boolean playersOnline) {
        this.actions = actions;
        this.playersOnline = playersOnline;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.playersOnline);
        friendlyByteBuf.writeInt(this.actions.size());
        for (PadAction action : this.actions) {
            action.encode(friendlyByteBuf);
        }
    }

    public static OpenActionPadPacket decode(FriendlyByteBuf friendlyByteBuf) {
        boolean playersOnline = friendlyByteBuf.readBoolean();
        int size = friendlyByteBuf.readInt();
        List<PadAction> actions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            actions.add(PadAction.decode(friendlyByteBuf));
        }

        return new OpenActionPadPacket(actions, playersOnline);
    }

    public static void handle(OpenActionPadPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ActionPadClient.openActionPadScreen(packet.actions, packet.playersOnline));
        context.get().setPacketHandled(true);
    }

}
