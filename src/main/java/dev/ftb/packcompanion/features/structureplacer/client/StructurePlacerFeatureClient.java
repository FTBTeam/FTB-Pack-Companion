package dev.ftb.packcompanion.features.structureplacer.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public class StructurePlacerFeatureClient {
    public static void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(PlacerRender::renderPlacerPreview);
        modEventBus.addListener(StructurePlacerFeatureClient::registerGuiLayers);
        modEventBus.addListener(StructurePlacerFeatureClient::onClientInit);
        modEventBus.addListener(PlacerKeys::onRegisterKeyBindings);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PlacerActionsOverlay.ID, PlacerActionsController.INSTANCE.overlay());
    }

    private static void onClientInit(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(PlacerKeys::onInputEvent);
        NeoForge.EVENT_BUS.addListener(StructurePlacerFeatureClient::onInputEvent);
    }

    public static void onInputEvent(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            PlacerActionsController.INSTANCE.onKeyDown(event.getKey());
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            PlacerActionsController.INSTANCE.onKeyUp(event.getKey());
        }
    }
}
