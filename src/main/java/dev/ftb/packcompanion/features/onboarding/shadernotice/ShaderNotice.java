package dev.ftb.packcompanion.features.onboarding.shadernotice;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.core.utils.ClientPersistentData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class ShaderNotice extends Feature.Client {
    public final ClientPersistentData shaderData = new ClientPersistentData("shader_notice");
    public final BooleanValue hasOnboarded = shaderData.data().addBoolean("has_onboarded", false);

    public ShaderNotice(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        shaderData.load();
        NeoForge.EVENT_BUS.addListener(this::playerLoggedIn);
    }

    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
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
}
