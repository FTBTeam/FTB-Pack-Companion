package dev.ftb.packcompanion.integrations.fancymenu;

import de.keksuccino.fancymenu.customization.loadingrequirement.LoadingRequirement;
import de.keksuccino.fancymenu.util.rendering.ui.screen.texteditor.TextEditorFormattingRule;
import dev.ftb.packcompanion.PackCompanion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.List;

public class HourLessThanReq extends LoadingRequirement {
    public HourLessThanReq() {
        super(PackCompanion.id("hour_less_than").toString().replace(":", "_")));
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
    public @NotNull String getDisplayName() {
        return "Hour Less Than";
    }

    @Override
    public @Nullable List<String> getDescription() {
        return List.of("Checks if the users current hour is less than the specified value.",
                "The value must be a number between 0 and 23, representing the hour in 24-hour format.");
    }

    @Override
    public @Nullable String getCategory() {
        return "FTB Pack Companion";
    }

    @Override
    public @Nullable String getValueDisplayName() {
        return "Hour (0-23)";
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
