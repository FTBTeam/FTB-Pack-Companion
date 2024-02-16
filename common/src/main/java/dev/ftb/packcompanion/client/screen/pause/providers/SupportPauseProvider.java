package dev.ftb.packcompanion.client.screen.pause.providers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SupportPauseProvider implements AdditionalPauseProvider {
    private static final ResourceLocation DISCORD_ICON = new ResourceLocation("ftbpc:textures/gui/discord.png");
    private static final ResourceLocation GITHUB_ICON = new ResourceLocation("ftbpc:textures/gui/github.png");

    @Override
    public @Nullable ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        return ScreenWidgetCollection.create()
                .addRenderableWidget(new IconButton(screen.unsafeScreenAccess(), x + 4, y + 4, 20, 20, DISCORD_ICON, "ftbpackcompanion.tooltip.support_discord", button -> {
                    Util.getPlatform().openUri("https://go.ftb.team/discord");
                }))
                .addRenderableWidget(new IconButton(screen.unsafeScreenAccess(), x + 4 + 24, y + 4, 20, 20, GITHUB_ICON, "ftbpackcompanion.tooltip.support_github", button -> {
                    Util.getPlatform().openUri("https://go.ftb.team/support-modpack");
                }));
    }

    private static class IconButton extends Button {
        private final ResourceLocation icon;
        private final OnTooltip customTooltip;

        public IconButton(Screen screen, int i, int j, int k, int l, ResourceLocation icon, String langKey, OnPress onPress) {
            super(i, j, k, l, Component.empty(), onPress);
            this.icon = icon;
            this.customTooltip = (button, poseStack, i1, j1) -> {
                screen.renderTooltip(poseStack, Component.translatable(langKey), i1, j1);
            };
        }

        @Override
        public void renderButton(PoseStack poseStack, int i, int j, float f) {
            super.renderButton(poseStack, i, j, f);

            RenderSystem.setShaderTexture(0, icon);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.pushPose();
            poseStack.translate(x + 3, y + 3, 0);
            poseStack.scale(.85f, .85f, 0);
            blit(poseStack, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();

            if (this.isHovered) {
                this.customTooltip.onTooltip(this, poseStack, i - 2, j + 12);
            }
        }
    }
}
