package dev.ftb.packcompanion.client.screen.pause;

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
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CustomPauseScreen extends Screen {
    public static boolean DISABLE_CUSTOM_PAUSE = false;

    // Stolen from MC
    private static final Component RETURN_TO_GAME = Component.translatable("menu.returnToGame");
    private static final Component ADVANCEMENTS = Component.translatable("gui.advancements");
    private static final Component STATS = Component.translatable("gui.stats");
    private static final Component OPTIONS = Component.translatable("menu.options");
    private static final Component SHARE_TO_LAN = Component.translatable("menu.shareToLan");
    private static final Component PLAYER_REPORTING = Component.translatable("menu.playerReporting");
    private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
    private static final Component GAME = Component.translatable("menu.game");
    private static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel");

    Minecraft minecraft = Minecraft.getInstance();
    ScreenHolder holder = ScreenHolder.of(this);

    @Nullable
    private Button disconnectButton;

    public CustomPauseScreen() {
        super(GAME);
    }

    @Override
    protected void init() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        rowHelper.addChild(Button.builder(RETURN_TO_GAME, button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).width(204).build(), 2, gridLayout.newCellSettings().paddingTop(50));

        var noAdvancements = PCClientConfig.REMOVE_ADVANCEMENTS_FROM_PAUSE.get();
        if (!noAdvancements) {
            rowHelper.addChild(this.openScreenButton(ADVANCEMENTS, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), this)));
        }

        rowHelper.addChild(this.openScreenButton(STATS, () -> new StatsScreen(this, this.minecraft.player.getStats())));

        if (PackCompanionExpectPlatform.hasModlistScreen()) {
            rowHelper.addChild(this.openScreenButton(Component.translatable("ftbpackcompanion.pause.mods", Platform.getMods().size()), () -> PackCompanionExpectPlatform.getModListScreen().apply(this), noAdvancements ? 98 : 204), noAdvancements ? 1 : 2);
        }

        rowHelper.addChild(this.openScreenButton(OPTIONS, () -> new OptionsScreen(this, this.minecraft.options)));
        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            rowHelper.addChild(this.openScreenButton(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
        } else {
            rowHelper.addChild(this.openScreenButton(PLAYER_REPORTING, () -> new SocialInteractionsScreen(this)));
        }

        Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT;
        this.disconnectButton = rowHelper.addChild(Button.builder(component, button -> {
            button.active = false;
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::onDisconnect, true);
        }).width(204).build(), 2);
        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
        gridLayout.visitWidgets(this::addRenderableWidget);

        // NOTE: Only needed for testing
//        this.addRenderableWidget(Button.builder(Component.literal("Original main menu"), (buttonx) -> {
//            DISABLE_CUSTOM_PAUSE = true;
//            this.minecraft.setScreen(new PauseScreen(true));
//        }).bounds(0, 0, 80, 20).build());

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

    private Button openScreenButton(Component component, Supplier<Screen> supplier) {
        return openScreenButton(component, supplier, 98);
    }

    private Button openScreenButton(Component component, Supplier<Screen> supplier, int width) {
        return Button.builder(component, button -> this.minecraft.setScreen(supplier.get())).width(width).build();
    }

    private void onDisconnect() {
        boolean bl = this.minecraft.isLocalServer();
        ServerData serverData = this.minecraft.getCurrentServer();
        this.minecraft.level.disconnect();
        if (bl) {
            this.minecraft.disconnect(new GenericMessageScreen(SAVING_LEVEL));
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
