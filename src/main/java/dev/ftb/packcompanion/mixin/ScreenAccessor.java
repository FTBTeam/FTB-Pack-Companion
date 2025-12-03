package dev.ftb.packcompanion.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor @Mutable
    List<GuiEventListener> getChildren();

    @Accessor @Mutable
    void setChildren(List<GuiEventListener> children);

    @Accessor @Mutable
    List<NarratableEntry> getNarratables();

    @Accessor @Mutable
    void setNarratables(List<NarratableEntry> narratables);
}
