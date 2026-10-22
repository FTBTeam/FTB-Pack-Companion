package dev.ftb.packcompanion.features.structureplacer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

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

        if (instance.screen != null) {
            return;
        }

        var playerPlacerItem = PlacerItem.getPlacerItemStack(player);
        if (playerPlacerItem.isEmpty()) {
            return;
        }

        Optional<ProcessedStructureTemplate> structure = ((PlacerItem) playerPlacerItem.get().getItem()).getStructureClient(playerPlacerItem.get(), player.level());
        if (structure.isEmpty()) {
            return;
        }

        var screenCenterX = instance.getWindow().getGuiScaledWidth() / 2;
        var screenBottom = instance.getWindow().getGuiScaledHeight();

        // The bottom of the screen allowing for the hotbar, health, and the hint text
        var y = screenBottom - 90;

        // Draw the overlay text
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(screenCenterX, y, 0);
        pose.scale(0.85f, 0.85f, 1f);
        if (controller.isFocused()) {
            drawStringWithBackground(guiGraphics, Component.translatable("ftbpackcompanion.structureplacer.nudge_x_z"), 0, -(16 * 2), 0xFFFFFF);
            drawStringWithBackground(guiGraphics, Component.translatable("ftbpackcompanion.structureplacer.nudge_y"), 0, -16, 0xFFFFFF);
        } else {
            drawStringWithBackground(guiGraphics, Component.translatable("ftbpackcompanion.structureplacer.nudge_hint"), 0, -16, 0xFFFFFF);
        }

        drawStringWithBackground(guiGraphics, Component.translatable(
                "ftbpackcompanion.structureplacer.anchor_rotate",
                PlacerKeys.ANCHOR_POS_KEY.getTranslatedKeyMessage().getString(),
                PlacerKeys.ROTATE_POS_KEY.getTranslatedKeyMessage().getString()
        ), 0, 0, 0xFFFFFF);
        pose.popPose();
    }

    private void drawStringWithBackground(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        var font = Minecraft.getInstance().font;
        var textWidth = font.width(text);
        var textHeight = font.lineHeight;

        var xOffset = textWidth / 2;
        x -= xOffset;

        int padding = 4;
        guiGraphics.fill(x - padding, y - padding, x + textWidth + padding - 1, y + textHeight + padding - 1, 0x80000000);

        // Draw the text
        guiGraphics.drawString(font, text, x, y, color);
    }
}
