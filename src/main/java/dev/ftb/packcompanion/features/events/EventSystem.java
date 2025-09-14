package dev.ftb.packcompanion.features.events;

import dev.ftb.packcompanion.core.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class EventSystem extends Feature.Common {
    public EventSystem(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    public void onServerTick(ServerTickEvent.Post event) {
        EventManager.INSTANCE.runEventLoop(event.getServer());
    }
}
