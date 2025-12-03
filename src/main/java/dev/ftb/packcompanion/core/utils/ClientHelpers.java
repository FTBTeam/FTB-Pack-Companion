package dev.ftb.packcompanion.core.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ClientHelpers {
    public static boolean clientIsSurvival() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new IllegalStateException("This method can only be called on the client side.");
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive()) {
            return false;
        }

        return !player.isCreative() && !player.isSpectator();
    }
}
