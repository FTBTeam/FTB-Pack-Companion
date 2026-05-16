package dev.ftb.packcompanion.integrations.fancymenu;

import de.keksuccino.fancymenu.customization.requirement.RequirementRegistry;

public class FancyMenuIntegration {
    public static void init() {
        RequirementRegistry.register(new HourGreaterThanReq());
        RequirementRegistry.register(new HourLessThanReq());
    }
}
