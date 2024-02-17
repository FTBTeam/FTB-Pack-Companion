package dev.ftb.packcompanion.fabric.integrations;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration {

    public static Screen createModListScreen(Screen parent) {
        return new ModsScreen(parent);
    }
}
