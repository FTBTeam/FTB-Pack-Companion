package dev.ftb.packcompanion.api.client.pause;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public record ScreenWidgetCollection(
        List<Widget> renderables,
        List<NarratableEntry> narratables,
        List<GuiEventListener> children
) {
    public static ScreenWidgetCollection create() {
        return new ScreenWidgetCollection(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    }

    public <T extends GuiEventListener & Widget & NarratableEntry> ScreenWidgetCollection addRenderableWidget(T guiEventListener) {
        this.renderables.add(guiEventListener);
        this.addWidget(guiEventListener);

        return this;
    }

    public <T extends Widget> ScreenWidgetCollection addRenderableOnly(T widget) {
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
        screen.narratables.addAll(this.narratables);
        screen.children.addAll(this.children);
    }
}
