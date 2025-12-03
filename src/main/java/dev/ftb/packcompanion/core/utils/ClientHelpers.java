package dev.ftb.packcompanion.core.utils;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ClientHelpers {
    public static boolean clientIsSurvival() {
        if (Platform.getEnv() != EnvType.CLIENT) {
            throw new IllegalStateException("This method can only be called on the client side.");
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive()) {
            return false;
        }

        return !player.isCreative() && !player.isSpectator();
    }
}
