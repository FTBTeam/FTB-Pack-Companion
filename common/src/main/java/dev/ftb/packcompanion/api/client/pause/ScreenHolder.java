package dev.ftb.packcompanion.api.client.pause;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a screen to provide read-only access to its internals.
 */
public class ScreenHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenHolder.class);

    private final Screen screen;

    private ScreenHolder(Screen screen) {
        this.screen = screen;
    }

    public static ScreenHolder of(Screen screen) {
        return new ScreenHolder(screen);
    }

    /**
     * Please do not use this method unless you absolutely have to. This is only provided for screens that require it
     * to return back to the previous screen.
     *
     * @return the wrapped screen
     */
    public final Screen unsafeScreenAccess() {
        return this.screen;
    }

    public ImmutableCollection<Widget> renderables() {
        return ImmutableList.copyOf(this.screen.renderables);
    }

    public ImmutableCollection<NarratableEntry> narratables() {
        return ImmutableList.copyOf(this.screen.narratables);
    }

    public ImmutableCollection<GuiEventListener> children() {
        return ImmutableList.copyOf(this.screen.children);
    }

    public int getWidth() {
        return screen.width;
    }

    public int getHeight() {
        return screen.height;
    }

    public Font font() {
        return Minecraft.getInstance().font;
    }

    public ItemRenderer itemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }
}
