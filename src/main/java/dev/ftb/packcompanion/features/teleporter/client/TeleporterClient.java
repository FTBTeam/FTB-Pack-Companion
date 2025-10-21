package dev.ftb.packcompanion.features.teleporter.client;

import dev.ftb.packcompanion.features.teleporter.TeleporterAction;
import dev.ftb.packcompanion.features.teleporter.net.TryOpenTeleporterFromItemPacket;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TeleporterClient {
    public static final KeyMapping OPEN_TELEPORTER_KEY = new KeyMapping(
            "ftbpackcompanion.key.open_teleporter",
            GLFW.GLFW_KEY_B,
            "ftbpackcompanion.key.category"
    );

    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TELEPORTER_KEY);
    }

    public static void onInputEvent(InputEvent.Key event) {
        if (OPEN_TELEPORTER_KEY.consumeClick()) {
            // Figure it out basically, let the server do the hard work and tell us what to do
            PacketDistributor.sendToServer(new TryOpenTeleporterFromItemPacket());
        }
    }

    public static void openTeleporterScreen(List<TeleporterAction> destinations) {
        new TeleporterScreen(destinations).openGuiLater();
    }
}
