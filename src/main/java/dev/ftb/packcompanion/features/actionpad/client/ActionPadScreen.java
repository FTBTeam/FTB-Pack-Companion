package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.packcompanion.features.actionpad.ActionPadFeature;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.net.RunActionPacket;
import dev.ftb.packcompanion.features.actionpad.net.TryOpenActionTPAPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class ActionPadScreen extends AbstractButtonListScreen {
    private final List<PadAction> actions;
    private final boolean playersOnline;

    public ActionPadScreen(List<PadAction> actions, boolean playersOnline) {
        super();

        this.showBottomPanel(false);
        this.setHasSearchBox(true);
        this.setTitle(ActionPadFeature.ACTION_PAD.get().getName(new ItemStack(ActionPadFeature.ACTION_PAD.get())));

        this.actions = actions;
        this.playersOnline = playersOnline;
    }

    @Override
    public boolean onInit() {
        int max = getTheme().getStringWidth(getTitle());
        for (PadAction a : actions) {
            max = Math.max(max, getTheme().getStringWidth(Component.translatable(a.name())) + 20);
        }

        setWidth(Mth.clamp(max, 150, this.getScreen().getGuiScaledWidth() * 3 / 4));
        return true;
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();
        setHeight(Mth.clamp(24 + getTopPanelHeight() + actions.size() * 20, 50, getScreen().getGuiScaledHeight() * 3 / 4));
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
                    ClientPacketDistributor.sendToServer(TryOpenActionTPAPacket.INSTANCE);
                }
            });
        }

        for (var action : actions) {
            panel.add(new SimpleTextButton(panel, Component.translatable(action.name()), action.icon()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    ClientPacketDistributor.sendToServer(new RunActionPacket(action.name()));
                    if (action.autoclose()) {
                        closeGui();
                    }
                }
            });
        }
    }
}
