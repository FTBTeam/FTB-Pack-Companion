package dev.ftb.packcompanion.integrations;

public interface IntegrationsEntrypoint {
    void onCommonInit();

    void onServerInit();

    void onClientInit();
}
