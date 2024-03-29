package dev.ftb.packcompanion.integrations.minetogether;

import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.gui.FriendChatGui;
import net.creeperhost.minetogether.chat.gui.PublicChatGui;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.creeperhost.minetogether.connect.gui.GuiShareToFriends;
import net.creeperhost.minetogether.gui.SettingGui;
import net.creeperhost.minetogether.polylib.gui.IconButton;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

public class MineTogetherPauseProvider implements AdditionalPauseProvider {
    @Override
    public ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        var collection = ScreenWidgetCollection.create();

        int xOffset = 0;
        if (integratedServer != null) {
            boolean isConnectPublished = ConnectHandler.isPublished();
            Component buttonText = isConnectPublished ? Component.translatable("minetogether.connect.close") : Component.translatable("minetogether.connect.open");
            Button.OnPress action = button -> {
                if (isConnectPublished) {
                    ConnectHandler.unPublish();
                    Minecraft.getInstance().setScreen(new PauseScreen(true));
                } else {
                    Minecraft.getInstance().setScreen(new GuiShareToFriends(screen.unsafeScreenAccess()));
                }
            };

            xOffset += 98;
            collection.addRenderableWidget(Button.builder(buttonText, action).bounds(x - xOffset, y, 98, 20).build());
        }

        xOffset += 22;
        IconButton settings = new IconButton(x - xOffset, y, 3, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new SettingGui(), screen.unsafeScreenAccess())));
        settings.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.settings.info")));
        collection.addRenderableWidget(settings);

        xOffset += 22;
        IconButton friendChat = new IconButton(x - xOffset, y, 7, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(new FriendChatGui(), screen.unsafeScreenAccess())));
        friendChat.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.friends.info")));
        collection.addRenderableWidget(friendChat);

        if (Config.instance().chatEnabled) {
            xOffset += 22;
            IconButton publicChat = new IconButton(x - xOffset, y, 1, Constants.WIDGETS_SHEET, e -> Minecraft.getInstance().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), screen.unsafeScreenAccess())));
            publicChat.setTooltip(Tooltip.create(Component.translatable("minetogether:gui.button.global_chat.info")));
            collection.addRenderableWidget(publicChat);
        }

        return collection;
    }
}
