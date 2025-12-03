package dev.ftb.packcompanion.features.actionpad.net;

import dev.ftb.packcompanion.features.actionpad.PadAction.ActionRunner;
import dev.ftb.packcompanion.features.actionpad.PadActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RunActionPacket {
    String actionName;

    public RunActionPacket(String actionName) {
        this.actionName = actionName;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(actionName);
    }

    public static RunActionPacket decode(FriendlyByteBuf friendlyByteBuf) {
        String actionName = friendlyByteBuf.readUtf();
        return new RunActionPacket(actionName);
    }

    public static void handle(RunActionPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context networkContext = context.get();
        var player = networkContext.getSender();

        if (player != null) {
            networkContext.enqueueWork(() ->
                    PadActions.get().getAction(player, packet.actionName)
                            .flatMap(action -> action.commandAction().map(ActionRunner::asActionRunner)
                                    .or(() -> action.teleportAction().map(ActionRunner::asActionRunner))
                            )
                            .ifPresent(actionRunner -> actionRunner.run(player)));
        }

        networkContext.setPacketHandled(true);
    }
}
