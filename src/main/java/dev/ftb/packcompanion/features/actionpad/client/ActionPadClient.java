package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.utils.NameAndUuid;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.net.TryOpenActionPadFromItemPacket;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ActionPadClient {
    public static final KeyMapping OPEN_ACTION_PAD_KEY = new KeyMapping(
            "ftbpackcompanion.key.open_action_pad",
            GLFW.GLFW_KEY_B,
            "ftbpackcompanion.key.category"
    );

    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ACTION_PAD_KEY);
    }

    public static void onInputEvent(InputEvent.Key event) {
        if (OPEN_ACTION_PAD_KEY.consumeClick()) {
            // Figure it out basically, let the server do the hard work and tell us what to do
            PacketDistributor.sendToServer(TryOpenActionPadFromItemPacket.INSTANCE);
        }
    }

    public static void openActionPadScreen(List<PadAction> actions, boolean playersOnline) {
        new ActionPadScreen(actions, playersOnline).openGuiLater();
    }

    public static void openActionPadTpaScreen(List<NameAndUuid> users) {
        new ActionTPAScreen(users).openGuiLater();
    }
}
