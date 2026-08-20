package dev.ftb.packcompanion.features.structureplacer.client;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.StructurePlacerFeature;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class PlacerActionsOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation ID = PackCompanion.id("placer_actions_overlay");

    private final PlacerActionsController controller;

    public PlacerActionsOverlay(PlacerActionsController controller) {
        this.controller = controller;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft instance = Minecraft.getInstance();
        var player = instance.player;
        if (player == null) {
            return;
        }

        var playerPlacerItem = PlacerItem.getPlacerItemStack(player);
        if (playerPlacerItem.isEmpty()) {
            return;
        }

        if (!playerPlacerItem.get().has(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT_TYPE.get())) {
            return;
        }

        var screenCenterX = instance.getWindow().getGuiScaledWidth() / 2;
        var screenBottom = instance.getWindow().getGuiScaledHeight();

        // The bottom of the screen allowing for the hotbar, health, and the hint text
        var y = screenBottom - 20 - 20 - 20;

        // TODO: Translations.
        // Draw the overlay text
        if (controller.isFocused()) {
            drawStringWithBackground(guiGraphics, "[W] Forwards | [S] Backwards | [A] Left | [D] Right", screenCenterX, y-(16 * 2), 0xFFFFFF);
            drawStringWithBackground(guiGraphics, "[Q] Up | [E] Down | [R] Reset", screenCenterX, y-16, 0xFFFFFF);
            drawStringWithBackground(guiGraphics, "[,] Anchor | [.] Rotate", screenCenterX, y, 0xFFFFFF);
        } else {
            drawStringWithBackground(guiGraphics, "Hold V to nudge", screenCenterX, y-16, 0xFFFFFF);
            drawStringWithBackground(guiGraphics, "[,] Anchor | [.] Rotate", screenCenterX, y, 0xFFFFFF);
        }
    }

    private void drawStringWithBackground(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        var font = Minecraft.getInstance().font;
        var textWidth = font.width(text);
        var textHeight = font.lineHeight;

        // Offset the x for a "centered" effect
        var xOffset = textWidth / 2;
        x -= xOffset;

        // Draw background rectangle
        int padding = 4;
        guiGraphics.fill(x - padding, y - padding, x + textWidth + padding - 1, y + textHeight + padding - 1, 0x80000000);

        // Draw the text
        guiGraphics.drawString(font, text, x, y, color);
    }
}
