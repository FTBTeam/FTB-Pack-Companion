package dev.ftb.packcompanion.neoforge.integrations;

import dev.ftb.packcompanion.neoforge.integrations.create.CreateIntegration;
import dev.ftb.packcompanion.integrations.IntegrationsCommon;
import dev.ftb.packcompanion.integrations.IntegrationsEntrypoint;

public class IntegrationsForge implements IntegrationsEntrypoint {
    @Override
    public void onCommonInit() {

    }

    @Override
    public void onServerInit() {

    }

    @Override
    public void onClientInit() {
        IntegrationsCommon.loadIfPresent("create", () -> CreateIntegration::init);
    }
}
