package dev.ftb.packcompanion.features.onboarding.shadernotice.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.onboarding.shadernotice.ShaderNoticeScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenShaderNoticePacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenShaderNoticePacket> TYPE = new CustomPacketPayload.Type<>(PackCompanion.id("open_shader_notice"));

    public static final StreamCodec<ByteBuf, OpenShaderNoticePacket> CODEC = StreamCodec.unit(new OpenShaderNoticePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOpen(final OpenShaderNoticePacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            new ShaderNoticeScreen().openGui();
        });
    }
}
