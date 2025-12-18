package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.net.RunActionPacket;
import dev.ftb.packcompanion.features.actionpad.net.TryOpenActionTPAPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ActionPadScreen extends AbstractButtonListScreen {
    private final List<PadAction> actions;
    private final boolean playersOnline;

    public ActionPadScreen(List<PadAction> actions, boolean playersOnline) {
        super();

        this.showBottomPanel(false);
        this.setHasSearchBox(true);
        this.setTitle(ActionPadFeature.ACTION_PAD.get().getDescription());

        this.actions = actions;
        this.playersOnline = playersOnline;
    }

    @Override
    public boolean onInit() {
        int max = getTheme().getStringWidth(getTitle());
        for (PadAction a : actions) {
            max = Math.max(max, getTheme().getStringWidth(Component.translatable(a.name())) + 20);
        }

        setSize(
                Mth.clamp(max, 150, getWindow().getGuiScaledWidth() * 3 / 4),
                Mth.clamp(20 + getTopPanelHeight() + (actions.size() + 1) * 20, 50, getWindow().getGuiScaledHeight() * 3 / 4)
        );
        return true;
    }

    @Override
    protected void doCancel() {}

    @Override
    protected void doAccept() {}

    @Override
    public void addButtons(Panel panel) {
        if (playersOnline) {
            panel.add(new SimpleTextButton(panel, Component.translatable("ftbpackcompanion.tpa"), Icons.MARKER) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    PacketDistributor.sendToServer(TryOpenActionTPAPacket.INSTANCE);
                }
            });
        }

        for (var action : actions) {
            panel.add(new SimpleTextButton(panel, Component.translatable(action.name()), action.icon()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    PacketDistributor.sendToServer(new RunActionPacket(action.name()));
                    if (action.autoclose()) {
                        closeGui();
                    }
                }
            });
        }
    }
}
