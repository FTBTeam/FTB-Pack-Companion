package dev.ftb.packcompanion.features.onboarding.shadernotice;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.core.utils.ClientPersistentData;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ShaderNotice extends Feature.Client {
    public final ClientPersistentData shaderData = new ClientPersistentData("shader_notice");
    public final BooleanValue hasOnboarded = shaderData.data().addBoolean("has_onboarded", false);

    // This is only on the client so we don't need to worry about other players here.
    private boolean pendingCheck = false;

    public ShaderNotice(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        shaderData.load();
    }

    @Override
    public void onClientInit() {
        NeoForge.EVENT_BUS.addListener(this::playerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::playerLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::clientTick);
    }

    private void playerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        pendingCheck = true;
    }

    private void playerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        pendingCheck = false;
    }

    private void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!pendingCheck || mc.player == null || mc.screen != null) {
            // Wait until the loading screen has closed, by which point the server config sync has been applied.
            return;
        }

        pendingCheck = false;

        if (!PCCommonConfig.SHOW_ON_START.get()) {
            return;
        }

        if (hasOnboarded.get()) {
            return;
        }

        new ShaderNoticeScreen(this).openGui();
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translation = collector.translationCollector();

        translation.prefixed("shaders_notice.title", "Would you like to use shaders?");
        translation.prefixed("shaders_notice.no_shaders.title", "No shaders");
        translation.prefixed("shaders_notice.shaders.title", "Shaders");
        translation.prefixed("shaders_notice.no_shaders.description", "Shaders have been included in this pack but are disabled by default. Would you like to enable them? Shaders can be performance-intensive, cause issues with certain mods, and may not be compatible with your hardware.");
        translation.prefixed("shaders_notice.shaders.description", "If you'd like shaders, you can enable them here to experience the pack with a vibrant look and feel. If you encounter any issues, you can always disable them later in the video settings.");
        translation.prefixed("shaders_notice.shaders_btn.disable", "Disable shaders");
        translation.prefixed("shaders_notice.shaders_btn.enable", "Enable shaders");
    }
}
