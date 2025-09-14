package dev.ftb.packcompanion.features.onboarding.shadernotice.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.onboarding.shadernotice.ShaderNotice;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SaveHasOnboardedPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SaveHasOnboardedPacket> TYPE = new CustomPacketPayload.Type<>(PackCompanion.id("save_has_onboarded"));
    public static final StreamCodec<ByteBuf, SaveHasOnboardedPacket> CODEC = StreamCodec.unit(new SaveHasOnboardedPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleSave(final SaveHasOnboardedPacket data, final IPayloadContext context) {
        CompoundTag persistentData = context.player().getPersistentData();
        persistentData.putBoolean(ShaderNotice.SHADER_NOTICE_TAG, true);
    }
}
