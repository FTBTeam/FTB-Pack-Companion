package dev.ftb.packcompanion.client.screen.pause.providers;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;
import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SupportPauseProvider implements AdditionalPauseProvider {
    private static final ResourceLocation DISCORD_ICON = PackCompanion.id("textures/gui/discord.png");
    private static final ResourceLocation GITHUB_ICON = PackCompanion.id("textures/gui/github.png");

    @Override
    public @Nullable ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        if (!PCClientConfig.ENABLE_SUPPORT_PROVIDER.get()) {
            return null;
        }

        ScreenWidgetCollection screenWidgetCollection = ScreenWidgetCollection.create();

        String githubUrl = PCClientConfig.SUPPORT_GITHUB_URL.get();
        String discordUrl = PCClientConfig.SUPPORT_DISCORD_URL.get();

        int xOffset = x;
        if (!discordUrl.isEmpty()) {
            screenWidgetCollection.addRenderableWidget(new IconButton(xOffset, y, 20, 20, DISCORD_ICON, "ftbpackcompanion.tooltip.support_discord", button -> {
                Util.getPlatform().openUri(discordUrl);
            }));

            xOffset += 24;
        }

        if (!githubUrl.isEmpty()) {
            screenWidgetCollection.addRenderableWidget(new IconButton(xOffset, y, 20, 20, GITHUB_ICON, "ftbpackcompanion.tooltip.support_github", button -> {
                Util.getPlatform().openUri(githubUrl);
            }));
        }

        return screenWidgetCollection;
    }

    public static class IconButton extends Button {
        private final ResourceLocation icon;
        private int textureSheetWidth = 16;
        private int textureSheetHeight = 16;
        private int uvx = 0;
        private int uvy = 0;
        private int textureWidth = 16;
        private int textureHeight = 16;
        private int hoverUVYOffset = 0;
        private boolean hasBackground = true;

        public IconButton(int x, int y, int width, int height, ResourceLocation icon, String langKey, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, (c) -> Component.translatable(langKey));
            this.icon = icon;

            this.setTooltip(Tooltip.create(Component.translatable(langKey)));
        }

        public IconButton(int x, int y, int width, int height, ResourceLocation icon, String langKey, OnPress onPress, int uvx, int uvy, int textureSheetWidth, int textureSheetHeight, int textureWidth, int textureHeight, int hoverUVYOffset, boolean hasBackground) {
            super(x, y, width, height, Component.empty(), onPress, (c) -> Component.translatable(langKey));
            this.icon = icon;

            this.setTooltip(Tooltip.create(Component.translatable(langKey)));
            this.uvx = uvx;
            this.uvy = uvy;
            this.textureSheetWidth = textureSheetWidth;
            this.textureSheetHeight = textureSheetHeight;
            this.hasBackground = hasBackground;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.hoverUVYOffset = hoverUVYOffset;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            super.renderWidget(guiGraphics, i, j, f);

            var poseStack = guiGraphics.pose();
            if (hasBackground) {
                poseStack.pushPose();
                poseStack.translate(this.getX() + 3, this.getY() + 3, 0);
                poseStack.scale(.85f, .85f, 0);
                guiGraphics.blit(this.icon, 0, 0, uvx, uvy, textureWidth, textureHeight, textureSheetWidth, textureSheetHeight);
                poseStack.popPose();
            } else {
                // ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight
                var hoverOffset = this.isHovered() ? hoverUVYOffset : 0;
                guiGraphics.blit(this.icon, this.getX(), this.getY(), uvx, uvy + hoverOffset, textureWidth, textureHeight, textureSheetWidth, textureSheetHeight);
            }
        }
    }
}
