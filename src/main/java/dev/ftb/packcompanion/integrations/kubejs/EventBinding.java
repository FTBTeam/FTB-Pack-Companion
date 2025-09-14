package dev.ftb.packcompanion.integrations.kubejs;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.packcompanion.features.events.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class EventBinding {
    /**
     * Provides easy access to the EventManager singleton.
     * @return The EventManager instance.
     */
    public EventManager manager() {
        return EventManager.INSTANCE;
    }

    /**
     * Easy access to an EventConfig builder for creating event configurations.
     * @return A new EventConfig.Builder instance.
     */
    public EventConfig.Builder configBuilder() {
        return EventConfig.builder();
    }

    public void registerCategory(ResourceLocation id, String name, String description, @Nullable Icon icon) {
        manager().registerCategory(id, name, description, icon);
    }

    public void registerEvent(ResourceLocation categoryId, Event event) {
        manager().registerEvent(categoryId, event);
    }

    /**
     * Helper method to register an abstract event created via magic from KubeJS.
     *
     * @param categoryId the id of the category to register the event under
     * @param id the id of the event
     * @param name the name of the event
     * @param description the description of the event
     * @param chance the chance of the event occurring (0.0 - 1.0)
     * @param config the configuration for the event
     * @param action the action to perform when the event is triggered
     */
    public void registerEvent(ResourceLocation categoryId, ResourceLocation id, String name, String description, double chance, EventConfig config, Consumer<EventContext> action) {
        manager().registerEvent(categoryId, new KubeCreatedEvent(id, name, description, chance, config, action));
    }
}
