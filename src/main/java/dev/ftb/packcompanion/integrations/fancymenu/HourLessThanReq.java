package dev.ftb.packcompanion.integrations.fancymenu;

import de.keksuccino.fancymenu.customization.requirement.Requirement;
import de.keksuccino.fancymenu.util.rendering.ui.screen.texteditor.TextEditorFormattingRule;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalTime;
import java.util.List;

public class HourLessThanReq extends Requirement {
    public HourLessThanReq() {
        super(PackCompanion.id("hour_less_than").toString().replace(":", "_"));
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean isRequirementMet(@Nullable String input) {
        var number = input != null ? Integer.parseInt(input) : -1;
        if (number == -1) {
            return false;
        }

        var currentHour = LocalTime.now().getHour();
        return currentHour < number;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("ftbpc.fancymenu.hourless.name");
    }

    @Override
    public @Nullable Component getDescription() {
        return Component.translatable("ftbpc.fancymenu.hourless.description");
    }

    @Override
    public @Nullable String getCategory() {
        return "FTB Pack Companion";
    }

    @Override
    public @Nullable Component getValueDisplayName() {
        return Component.translatable("ftbpc.fancymenu.hour.value.name");
    }

    @Override
    public @Nullable String getValuePreset() {
        return null;
    }

    @Override
    public @Nullable List<TextEditorFormattingRule> getValueFormattingRules() {
        return List.of();
    }
}
