package dev.ftb.packcompanion.features.structureplacer.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.DropDownMenu;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.network.GetStructureIdsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PlacerItemConfigureScreen extends BaseScreen {
    private static List<ResourceLocation> availableStructureIds = new ArrayList<>();
    private static Instant lastStructureIdRequest = Instant.EPOCH;

    private String structureId = "";
    private TextBox structureIdBox;

    public PlacerItemConfigureScreen(ItemStack clientItemStack) {
        // Try to request structure IDs from the server, it might not be needed but won't hurt
        requestStructureIds();

        var structureIdFromItem = clientItemStack.getItem() instanceof PlacerItem ? PlacerItem.getStructureIdFromItem(clientItemStack) : null;
        if (structureIdFromItem != null) {
            this.structureId = structureIdFromItem.toString();
        }
    }

    @Override
    public void addWidgets() {
        structureIdBox = new TextBox(this) {
            @Override
            public void onTextChanged() {
                var attemptId = ResourceLocation.tryParse(getText());
                if (attemptId != null) {
                    structureId = attemptId.toString();
                }
            }
        };

        structureIdBox.setLabel(Component.literal("Structure ID"));
        structureIdBox.setText(structureId);
        structureIdBox.setSize(100, 16);
        add(structureIdBox);

        DropDownMenu dropDownMenu = new DropDownMenu(this, availableStructureIds.stream().map(e -> new ContextMenuItem(Component.literal(e.toString()), Icon.empty(), (b) -> {
            this.structureId = e.toString();
        })).toList());
        dropDownMenu.setSize(100, 100);
        add(dropDownMenu);
    }

    @Override
    public void alignWidgets() {
        this.align(WidgetLayout.VERTICAL);
    }

    public static void requestStructureIds() {
        var now = Instant.now();
        // Throttle requests to once every 5 minutes
        if (now.isAfter(lastStructureIdRequest.plusSeconds(5 * 60))) {
            lastStructureIdRequest = now;
            PacketDistributor.sendToServer(new GetStructureIdsPacket());
        }
    }

    public static void updateAvailableStructureIds(List<ResourceLocation> structureIds) {
        availableStructureIds = structureIds;
        lastStructureIdRequest = Instant.now();

        if (Minecraft.getInstance().screen instanceof ScreenWrapper screenWrapper && screenWrapper.getGui() instanceof PlacerItemConfigureScreen placerScreen) {
            // Trigger a refresh of the widgets to update any dropdowns or suggestions
            placerScreen.refreshWidgets();
        }
    }
}
