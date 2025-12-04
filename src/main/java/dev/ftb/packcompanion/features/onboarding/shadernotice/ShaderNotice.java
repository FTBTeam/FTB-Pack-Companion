package dev.ftb.packcompanion.features.onboarding.shadernotice;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.core.utils.ClientPersistentData;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;

public class ShaderNotice extends Feature.Client {
    public final ClientPersistentData shaderData = new ClientPersistentData("shader_notice");
    public final BooleanValue hasOnboarded = shaderData.data().addBoolean("has_onboarded", false);

    public ShaderNotice(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        shaderData.load();
    }

    @Override
    public void onClientInit() {
        MinecraftForge.EVENT_BUS.addListener(this::playerLoggingEvent);
    }

    public void playerLoggingEvent(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!PCCommonConfig.SHOW_ON_START.get()) {
            // Don't show the notice if the option is disabled.
            return;
        }

        if (hasOnboarded.get()) {
            // Don't show the notice if the player has already onboarded.
            return;
        }

        new ShaderNoticeScreen(this).openGuiLater();
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
