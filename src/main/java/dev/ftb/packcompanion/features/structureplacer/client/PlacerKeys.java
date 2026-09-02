package dev.ftb.packcompanion.features.structureplacer.client;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.PackCompanionClient;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.network.AnchorPlacerPacket;
import dev.ftb.packcompanion.features.structureplacer.network.RotatePlacerPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class PlacerKeys {
    public static final KeyMapping ANCHOR_POS_KEY = new KeyMapping("ftbpackcompanion.key.anchor_pos", GLFW.GLFW_KEY_COMMA, PackCompanionClient.getKeyCategory());
    public static final KeyMapping ROTATE_POS_KEY = new KeyMapping("ftbpackcompanion.key.rotate_pos", GLFW.GLFW_KEY_PERIOD, PackCompanionClient.getKeyCategory());

    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(ANCHOR_POS_KEY);
        event.register(ROTATE_POS_KEY);
    }

    public static void onInputEvent(InputEvent.Key event) {
        // Return early so we don't evaluate every keypress, only the ones we care about.
        if (event.getKey() != ANCHOR_POS_KEY.getKey().getValue() && event.getKey() != ROTATE_POS_KEY.getKey().getValue()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // Only a client-side check, We don't trust this and we reverify on the packet.
        var itemInHand = PlacerItem.getPlacerItemStack(player);
        if (itemInHand.isEmpty()) {
            return;
        }

        if (ANCHOR_POS_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(AnchorPlacerPacket.INSTANCE);
        } else if (ROTATE_POS_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new RotatePlacerPacket(player.isShiftKeyDown()));
        }
    }
}
