package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.net.RunActionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;

public class ActionPadScreen extends AbstractButtonListScreen {
    private final List<PadAction> actions;

    public ActionPadScreen(List<PadAction> actions) {
        super();

        this.showBottomPanel(false);
        this.setHasSearchBox(true);
        this.setTitle(ActionPadFeature.ACTION_PAD.get().getDescription());

        this.actions = actions;
    }

    @Override
    public boolean onInit() {
        int max = getTheme().getStringWidth(getTitle());
        for (PadAction a : actions) {
            max = Math.max(max, getTheme().getStringWidth(Component.translatable(a.name())) + 20);
        }

        setSize(
                Mth.clamp(max, 150, getScreen().getGuiScaledWidth() * 3 / 4),
                Mth.clamp(20 + getTopPanelHeight() + actions.size() * 20, 50, getScreen().getScreenHeight() * 3 / 4)
        );
        return true;
    }

    @Override
    protected void doCancel() {}

    @Override
    protected void doAccept() {}

    @Override
    public void addButtons(Panel panel) {
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
