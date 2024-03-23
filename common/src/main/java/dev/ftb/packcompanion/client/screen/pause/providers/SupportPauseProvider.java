package dev.ftb.packcompanion.client.screen.pause.providers;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;
import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SupportPauseProvider implements AdditionalPauseProvider {
    private static final ResourceLocation DISCORD_ICON = new ResourceLocation("ftbpc:textures/gui/discord.png");
    private static final ResourceLocation GITHUB_ICON = new ResourceLocation("ftbpc:textures/gui/github.png");

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
            screenWidgetCollection.addRenderableWidget(new IconButton(screen.unsafeScreenAccess(), xOffset, y, 20, 20, DISCORD_ICON, "ftbpackcompanion.tooltip.support_discord", button -> {
                Util.getPlatform().openUri(discordUrl);
            }));

            xOffset += 24;
        }

        if (!githubUrl.isEmpty()) {
            screenWidgetCollection.addRenderableWidget(new IconButton(screen.unsafeScreenAccess(), xOffset, y, 20, 20, GITHUB_ICON, "ftbpackcompanion.tooltip.support_github", button -> {
                Util.getPlatform().openUri(githubUrl);
            }));
        }

        return screenWidgetCollection;
    }

    private static class IconButton extends Button {
        private final ResourceLocation icon;

        public IconButton(Screen screen, int i, int j, int k, int l, ResourceLocation icon, String langKey, OnPress onPress) {
            super(i, j, k, l, Component.empty(), onPress, (c) -> Component.translatable(langKey));
            this.icon = icon;

            this.setTooltip(Tooltip.create(Component.translatable(langKey)));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            super.renderWidget(guiGraphics, i, j, f);

            RenderSystem.setShaderTexture(0, icon);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(this.getX() + 3, this.getY() + 3, 0);
            poseStack.scale(.85f, .85f, 0);
            guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }
    }
}
