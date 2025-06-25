package dev.ftb.packcompanion.integrations.fancymenu;

import de.keksuccino.fancymenu.customization.loadingrequirement.LoadingRequirementRegistry;

public class FancyMenuIntegration {
    public static void init() {
        LoadingRequirementRegistry.register(new HourGreaterThanReq());
        LoadingRequirementRegistry.register(new HourLessThanReq());
    }
}
