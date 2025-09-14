package dev.ftb.packcompanion.features.onboarding.setup;

import dev.ftb.packcompanion.core.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public class FirstTimeSetupFeature extends Feature.Common {
    public FirstTimeSetupFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }
}
