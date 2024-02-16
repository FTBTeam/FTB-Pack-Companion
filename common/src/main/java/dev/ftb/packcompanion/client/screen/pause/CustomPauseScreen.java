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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
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
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, Component.translatable("menu.returnToGame"), (buttonx) -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));

        var noAdvancements = PCClientConfig.REMOVE_ADVANCEMENTS_FROM_PAUSE.get();
        if (!noAdvancements) {
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 100, 20, Component.translatable("gui.advancements"), (buttonx) -> {
                this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()));
            }));
        }

        this.addRenderableWidget(new Button(noAdvancements ? this.width / 2 - 102 : (this.width / 2 + 2), this.height / 4 + 48 + -16, noAdvancements ? 204 : 100, 20, Component.translatable("gui.stats"), (buttonx) -> {
            this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()));
        }));

        if (PackCompanionExpectPlatform.hasModlistScreen()) {
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 204, 20, Component.translatable("ftbpackcompanion.pause.mods", Platform.getMods().size()), (buttonx) -> {
                this.minecraft.setScreen(PackCompanionExpectPlatform.getModListScreen().apply(this));
            }));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 100, 20, Component.translatable("menu.options"), (buttonx) -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));


        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 4 + 96 + -16, 100, 20, Component.translatable("menu.shareToLan"), (arg) -> {
                this.minecraft.setScreen(new ShareToLanScreen(this));
            }));
        } else {
            this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 4 + 96 + -16, 100, 20, Component.translatable("menu.playerReporting"), (arg) -> {
                this.minecraft.setScreen(new SocialInteractionsScreen());
            }));
        }

        Component component = this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect");
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, component, (buttonx) -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = this.minecraft.isConnectedToRealms();
            buttonx.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                this.minecraft.clearLevel();
            }

            TitleScreen titleScreen = new TitleScreen();
            if (bl) {
                this.minecraft.setScreen(titleScreen);
            } else if (bl2) {
                this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
            } else {
                this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
            }
        }));

        // NOTE: Only needed for testing
//        this.addRenderableWidget(new Button(0, 0, 80, 20, Component.literal("Original main menu"), (buttonx) -> {
//            DISABLE_CUSTOM_PAUSE = true;
//            this.minecraft.setScreen(new PauseScreen(true));
//        }));

        initAdditionalPauseProviders();
    }

    private void initAdditionalPauseProviders() {
        PackCompanionClientAPI.get().additionalPauseProviders.forEach((target, provider) -> {
            var location = calculatePosition(target);
            var widgetCollection = provider.init(target, holder, location[0], location[1]);
            if (widgetCollection != null) {
                widgetCollection.commitToScreen(this);
            }
        });
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float f) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);

        // TODO: position correctly based on the target
        PackCompanionClientAPI.get().additionalPauseProviders.forEach((target, provider) -> {
            int[] position = calculatePosition(target);

            provider.render(target, holder, poseStack, position[0], position[1], mouseX, mouseY, f);
        });
    }

    private int[] calculatePosition(AdditionalPauseTarget target) {
        return switch (target) {
            case TOP_LEFT -> new int[]{0, 0};
            case TOP_RIGHT -> new int[]{this.width, 0};
            case TOP_CENTER -> new int[]{this.width / 2, 0};
            case BOTTOM_LEFT -> new int[]{0, this.height};
            case BOTTOM_RIGHT -> new int[]{this.width, this.height};
            case BOTTOM_CENTER -> new int[]{this.width / 2, this.height};
            case MENU_LEFT -> new int[]{this.width / 2 - 102, this.height / 2};
            case MENU_RIGHT -> new int[]{this.width / 2 + 102, this.height / 2};
        };
    }
}
