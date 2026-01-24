package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.client.PlacerItemConfigureScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ProvideStructureIdsPacket(
        List<ResourceLocation> structureIds
) implements CustomPacketPayload {
    public static final Type<ProvideStructureIdsPacket> TYPE = new Type<>(PackCompanion.id("provide_structure_ids"));

    public static final StreamCodec<ByteBuf, ProvideStructureIdsPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), ProvideStructureIdsPacket::structureIds,
        ProvideStructureIdsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> PlacerItemConfigureScreen.updateAvailableStructureIds(structureIds));
    }
}
