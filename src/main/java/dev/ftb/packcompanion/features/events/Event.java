package dev.ftb.packcompanion.features.events;

import net.minecraft.resources.ResourceLocation;

public abstract class Event {
    /**
     * A unique identifier for the event
     */
    private final ResourceLocation id;

    /**
     * A display name, preferably a translation key, for the event
     */
    private final String name;

    /**
     * A display description, preferably a translation key, for the event
     */
    private final String description;

    private final EventConfig config;

    public Event(ResourceLocation id,  String name, String description, EventConfig config) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.config = config;
    }

    public abstract void action(EventContext context);

    /**
     * The chance of the event happening, the lower the number, the lower the chance. The high the number
     * the higher the chance. 1.0D is guaranteed to happen, 0.0D is guaranteed to not happen.
     */
    public double chance() {
        return 0.1D;
    }

    public ResourceLocation id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public EventConfig config() {
        return config;
    }
}
