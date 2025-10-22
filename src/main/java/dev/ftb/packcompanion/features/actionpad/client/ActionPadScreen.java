package dev.ftb.packcompanion.features.actionpad.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.packcompanion.features.actionpad.PadAction;
import dev.ftb.packcompanion.features.actionpad.net.RunActionPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ActionPadScreen extends AbstractButtonListScreen {
    private final List<PadAction> destinations;

    public ActionPadScreen(List<PadAction> destinations) {
        super();

        this.showBottomPanel(false);
        this.setHasSearchBox(true);
        this.setTitle(Component.literal("Destinations"));

        this.destinations = destinations;
    }

    @Override
    protected void doCancel() {}

    @Override
    protected void doAccept() {}

    @Override
    public void addButtons(Panel panel) {
        for (var destination : destinations) {
            panel.add(new SimpleTextButton(panel, Component.translatable(destination.name()), destination.icon()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    PacketDistributor.sendToServer(new RunActionPacket(destination));
                }
            });
        }
    }
}
