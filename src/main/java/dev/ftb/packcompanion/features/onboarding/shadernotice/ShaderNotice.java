package dev.ftb.packcompanion.features.onboarding.shadernotice;

import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.onboarding.shadernotice.network.OpenShaderNoticePacket;
import dev.ftb.packcompanion.features.onboarding.shadernotice.network.SaveHasOnboardedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ShaderNotice extends Feature.Common {
    public static final String SHADER_NOTICE_TAG = "ftbpc:has_onboarded_shaders";

    public ShaderNotice(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::playerLoggedIn);
    }

    @Override
    public void registerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(OpenShaderNoticePacket.TYPE, OpenShaderNoticePacket.CODEC, OpenShaderNoticePacket::handleOpen);
        registrar.playToServer(SaveHasOnboardedPacket.TYPE, SaveHasOnboardedPacket.CODEC, SaveHasOnboardedPacket::handleSave);
    }

    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            var persistentData = entity.getPersistentData();
            if (persistentData.getBoolean(SHADER_NOTICE_TAG)) {
                return;
            }

            PacketDistributor.sendToPlayer(serverPlayer, new OpenShaderNoticePacket());
        }
    }
}
