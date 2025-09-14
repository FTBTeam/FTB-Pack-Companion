package dev.ftb.packcompanion.features.events;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class KubeCreatedEvent extends Event {
    private final double chance;
    private final Consumer<EventContext> action;

    public KubeCreatedEvent(ResourceLocation id, String name, String description, double chance, EventConfig config, Consumer<EventContext> action) {
        super(id, name, description, config);
        this.action = action;
        this.chance = chance;
    }

    @Override
    public void action(EventContext context) {
        action.accept(context);
    }

    @Override
    public double chance() {
        return chance;
    }
}
