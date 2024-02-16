package dev.ftb.packcompanion.forge.integrations;

import dev.ftb.packcompanion.forge.integrations.create.CreateIntegration;
import dev.ftb.packcompanion.integrations.minetogether.MineTogetherIntegration;
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
