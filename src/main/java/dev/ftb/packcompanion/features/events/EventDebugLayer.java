package dev.ftb.packcompanion.features.events;

import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class EventDebugLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = PackCompanion.id("event_debug");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int debugX = 10;
        int debugY = 10;

        // We need the data from the server somehow xD... Fucking client renders man.
    }
}
