package dev.ftb.packcompanion.features.events;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public class EventClient {
    static void init(IEventBus modEventBus) {
        modEventBus.addListener(EventClient::registerGuiLayer);
    }

    public static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(EventDebugLayer.ID, new EventDebugLayer());
    }
}
