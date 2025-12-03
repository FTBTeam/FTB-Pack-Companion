package dev.ftb.packcompanion.api.client.pause;

import com.google.common.collect.Lists;
import dev.ftb.packcompanion.mixin.ScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public record ScreenWidgetCollection(
        List<Renderable> renderables,
        List<NarratableEntry> narratables,
        List<GuiEventListener> children
) {
    public static ScreenWidgetCollection create() {
        return new ScreenWidgetCollection(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> ScreenWidgetCollection addRenderableWidget(T guiEventListener) {
        this.renderables.add(guiEventListener);
        this.addWidget(guiEventListener);

        return this;
    }

    public <T extends Renderable> ScreenWidgetCollection addRenderableOnly(T widget) {
        this.renderables.add(widget);
        return this;
    }

    public <T extends GuiEventListener & NarratableEntry> ScreenWidgetCollection addWidget(T guiEventListener) {
        this.children.add(guiEventListener);
        this.narratables.add(guiEventListener);
        return this;
    }

    public void commitToScreen(Screen screen) {
        screen.renderables.addAll(this.renderables);

        List<NarratableEntry> internalNarratables = ((ScreenAccessor) screen).getNarratables();
        internalNarratables.addAll(this.narratables);

        List<GuiEventListener> internalChildren = ((ScreenAccessor) screen).getChildren();
        internalChildren.addAll(this.children);

        ((ScreenAccessor) screen).setNarratables(internalNarratables);
        ((ScreenAccessor) screen).setChildren(internalChildren);
    }
}
