package dev.ftb.packcompanion.client.screen.pause;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import dev.architectury.platform.Platform;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import dev.ftb.packcompanion.api.client.PackCompanionClientAPI;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.config.PCClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public class CustomPauseScreen extends Screen {
    public static boolean DISABLE_CUSTOM_PAUSE = false;

    Minecraft minecraft = Minecraft.getInstance();
    ScreenHolder holder = ScreenHolder.of(this);

    public CustomPauseScreen() {
        super(Component.translatable("menu.game"));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("menu.returnToGame"), (buttonx) -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).bounds(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20).build());

        var noAdvancements = PCClientConfig.REMOVE_ADVANCEMENTS_FROM_PAUSE.get();
        if (!noAdvancements) {
            this.addRenderableWidget(Button.builder( Component.translatable("gui.advancements"), (buttonx) -> {
                this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()));
            }).bounds(this.width / 2 - 102, this.height / 4 + 48 + -16, 100, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.stats"), (buttonx) -> {
            this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()));
        }).bounds(noAdvancements ? this.width / 2 - 102 : (this.width / 2 + 2), this.height / 4 + 48 + -16, noAdvancements ? 204 : 100, 20).build());

        if (PackCompanionExpectPlatform.hasModlistScreen()) {
            this.addRenderableWidget(Button.builder(Component.translatable("ftbpackcompanion.pause.mods", Platform.getMods().size()), (buttonx) -> {
                this.minecraft.setScreen(PackCompanionExpectPlatform.getModListScreen().apply(this));
            }).bounds(this.width / 2 - 102, this.height / 4 + 56, 204, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), (buttonx) -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }).bounds(this.width / 2 - 102, this.height / 4 + 96 + -16, 100, 20).build());


        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            this.addRenderableWidget(Button.builder(Component.translatable("menu.shareToLan"), (arg) -> {
                this.minecraft.setScreen(new ShareToLanScreen(this));
            }).bounds(this.width / 2 + 2, this.height / 4 + 96 + -16, 100, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("menu.playerReporting"), (arg) -> {
                this.minecraft.setScreen(new SocialInteractionsScreen());
            }).bounds(this.width / 2 + 2, this.height / 4 + 96 + -16, 100, 20).build());
        }

        Component component = this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect");
        this.addRenderableWidget(Button.builder(component, (buttonx) -> {
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, () -> {
                boolean bl = this.minecraft.isLocalServer();
                ServerData serverData = this.minecraft.getCurrentServer();
                this.minecraft.level.disconnect();
                if (bl) {
                    this.minecraft.disconnect(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
                } else {
                    this.minecraft.disconnect();
                }

                TitleScreen titleScreen = new TitleScreen();
                if (bl) {
                    this.minecraft.setScreen(titleScreen);
                } else if (serverData != null && serverData.isRealm()) {
                    this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
                } else {
                    this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
                }
            }, true);
        }).bounds(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20).build());

        // NOTE: Only needed for testing
//        this.addRenderableWidget(new Button(0, 0, 80, 20, Component.literal("Original main menu"), (buttonx) -> {
//            DISABLE_CUSTOM_PAUSE = true;
//            this.minecraft.setScreen(new PauseScreen(true));
//        }));

        initAdditionalPauseProviders();
    }

    private void initAdditionalPauseProviders() {
        PackCompanionClientAPI.get().getAdditionalPauseProviders().forEach((target, providers) -> {
            var location = calculatePosition(target);
            for (var provider : providers) {
                var widgetCollection = provider.init(target, holder, location[0], location[1]);
                if (widgetCollection != null) {
                    widgetCollection.commitToScreen(this);
                }
            }
        });
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        renderBackground(guiGraphics, mouseX, mouseY, f);
        super.render(guiGraphics, mouseX, mouseY, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);

        // TODO: position correctly based on the target
        PackCompanionClientAPI.get().getAdditionalPauseProviders().forEach((target, providers) -> {
            int[] position = calculatePosition(target);

            for (var provider : providers) {
                provider.render(target, holder, guiGraphics, position[0], position[1], mouseX, mouseY, f);
            }
        });
    }

    private int[] calculatePosition(AdditionalPauseTarget target) {
        return switch (target) {
            case TOP_LEFT -> new int[]{4, 4};
            case TOP_RIGHT -> new int[]{this.width - 4, 4};
            case TOP_CENTER -> new int[]{this.width / 2, 4};
            case BOTTOM_LEFT -> new int[]{4, this.height - 4};
            case BOTTOM_RIGHT -> new int[]{this.width - 4, this.height - 4};
            case BOTTOM_CENTER -> new int[]{this.width / 2, this.height - 4};
            case MENU_LEFT -> new int[]{this.width / 2 - 102 - 4, this.height / 4 + 56};
            case MENU_RIGHT -> new int[]{this.width / 2 + 102 + 4, this.height / 4 + 56};
        };
    }
}
