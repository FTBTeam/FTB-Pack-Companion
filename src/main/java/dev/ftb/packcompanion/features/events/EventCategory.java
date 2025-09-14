package dev.ftb.packcompanion.features.events;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record EventCategory(
        ResourceLocation id,
        String name,
        String description,
        @Nullable Icon icon
) {
}
