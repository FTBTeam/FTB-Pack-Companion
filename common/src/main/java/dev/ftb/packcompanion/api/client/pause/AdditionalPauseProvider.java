package dev.ftb.packcompanion.api.client.pause;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @implNote Please do NOT try and modify the screen. This api is provided to avoid nasty hacks
 *           and mutations of the original screen which has caused many issues in the past.
 */
public interface AdditionalPauseProvider {
    /**
     * Initialize the additional pause screen
     *
     * @param target the target this provider is being initialized for
     * @param screen a read-only screen delegate
     */
    @Nullable
    default ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        return null;
    }

    /**
     * Render the additional pause screen
     *
     * @param target the target this provider is being rendered for
     * @param screen the read-only screen
     * @param stack the pose stack
     * @param x the x position relative to the position this provider has been added to
     * @param y the y position relative to the position this provider has been added to
     * @param partialTicks the partial ticks
     */
    default void render(AdditionalPauseTarget target, ScreenHolder screen, PoseStack stack, int x, int y, int mouseX, int mouseY, float partialTicks) {
        // NO-OP
    }
}
