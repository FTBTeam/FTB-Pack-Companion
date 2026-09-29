package dev.ftb.packcompanion.features.structureplacer.client;

import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.network.NudgePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class PlacerActionsController {
    public static final PlacerActionsController INSTANCE = new PlacerActionsController();

    private final PlacerActionsOverlay overlay = new PlacerActionsOverlay(this);
    private boolean isFocused = false;

    public void onKeyDown(int key) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }

        // TODO: KeyMapping, not hardcoded.
        if (key == GLFW.GLFW_KEY_V) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            var hasPlacerItem = PlacerItem.getPlacerItemStack(player).isPresent();
            if (!hasPlacerItem) {
                return;
            }

            isFocused = true;
        }
    }


    // TODO: We need a way of blocking this from moving the player if we're focused
    public void onKeyUp(int key) {
        // We always reset focus on key up incase the player managed to get stuck into a focused state.
        // TODO: KeyMapping, not hardcoded.
        if (key == GLFW.GLFW_KEY_V) {
            isFocused = false;
        }

        if (!isFocused) {
            return;
        }

        if (key == GLFW.GLFW_KEY_W) {
            nudge(0, 0, 1); // Move forwards
        } else if (key == GLFW.GLFW_KEY_S) {
            nudge(0, 0, -1); // Move backwards
        } else if (key == GLFW.GLFW_KEY_A) {
            nudge(-1, 0, 0); // Move left
        } else if (key == GLFW.GLFW_KEY_D) {
            nudge(1, 0, 0); // Move right
        } else if (key == GLFW.GLFW_KEY_Q) {
            nudge(0, 1, 0); // Move up
        } else if (key == GLFW.GLFW_KEY_E) {
            nudge(0, -1, 0); // Move down
        } else if (key == GLFW.GLFW_KEY_R) {
            ClientPacketDistributor.sendToServer(new NudgePacket(0, 0, 0, true)); // Reset position
        }
    }

    private void nudge(int x, int y, int z) {
        if (!isFocused) {
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Direction forward = player.getDirection(); // nearest cardinal direction, snapped from yaw
        Direction right = forward.getClockWise();  // 90(deg) clockwise from forward

        int worldX = right.getStepX() * x + forward.getStepX() * z;
        int worldZ = right.getStepZ() * x + forward.getStepZ() * z;

        ClientPacketDistributor.sendToServer(new NudgePacket(worldX, y, worldZ));
    }

    public boolean isFocused() {
        return isFocused;
    }

    public PlacerActionsOverlay overlay() {
        return overlay;
    }
}
