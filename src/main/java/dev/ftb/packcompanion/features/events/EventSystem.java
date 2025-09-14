package dev.ftb.packcompanion.features.events;

import dev.ftb.packcompanion.core.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class EventSystem extends Feature.Common {
    private final IEventBus modEventBus;

    public EventSystem(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        this.modEventBus = modEventBus;

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        modEventBus.addListener(this::onClientSetup);
    }

    public void onServerTick(ServerTickEvent.Post event) {
        EventManager.INSTANCE.runEventLoop(event.getServer());
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        EventClient.init(this.modEventBus);
    }
}
