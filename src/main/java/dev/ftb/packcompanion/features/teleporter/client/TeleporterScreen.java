package dev.ftb.packcompanion.features.teleporter.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.packcompanion.features.teleporter.TeleporterAction;
import dev.ftb.packcompanion.features.teleporter.net.RunTeleporterAction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class TeleporterScreen extends AbstractButtonListScreen {
    private final List<TeleporterAction> destinations;

    public TeleporterScreen(List<TeleporterAction> destinations) {
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
                    PacketDistributor.sendToServer(new RunTeleporterAction(destination));
                }
            });
        }
    }
}
