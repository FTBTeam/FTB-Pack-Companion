package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.packcompanion.core.utils.NameAndUuid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ActionTPAScreen extends AbstractButtonListScreen {
    private final List<NameAndUuid> users;

    public ActionTPAScreen(List<NameAndUuid> users) {
        super();

        this.showBottomPanel(false);
        this.setHasSearchBox(true);
        this.setTitle(Component.translatable("ftbpackcompanion.tpa"));

        this.users = users;
    }

    @Override
    public boolean onInit() {
        int max = getTheme().getStringWidth(getTitle());
        for (NameAndUuid user : users) {
            max = Math.max(max, getTheme().getStringWidth(Component.translatable(user.name())) + 20);
        }

        setSize(
                Mth.clamp(max, 150, getWindow().getGuiScaledWidth() * 3 / 4),
                Mth.clamp(20 + getTopPanelHeight() + users.size() * 20, 50, getWindow().getGuiScaledHeight() * 3 / 4)
        );

        return true;
    }

    @Override
    public void addButtons(Panel panel) {
        for (var user : users) {
            panel.add(new SimpleTextButton(panel, Component.translatable(user.name()), Icon.empty()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        player.connection.sendCommand("tpa " + user.name());
                        SimpleToast.info(Component.translatable("ftbpackcompanion.actionpad.tpa.request_sent"), Component.literal(user.name()));
                    }
                    closeGui();
                }
            });
        }
    }

    @Override
    protected void doCancel() {}

    @Override
    protected void doAccept() {}
}
