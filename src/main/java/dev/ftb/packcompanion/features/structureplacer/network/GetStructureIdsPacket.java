package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record GetStructureIdsPacket() implements CustomPacketPayload {
    public static final GetStructureIdsPacket INSTANCE = new GetStructureIdsPacket();

    public static final Type<GetStructureIdsPacket> TYPE = new Type<>(PackCompanion.id("get_structure_ids"));

    public static final StreamCodec<ByteBuf, GetStructureIdsPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerLevel level = (ServerLevel) context.player().level();

            List<ResourceLocation> structureIds = level.getStructureManager().listTemplates().toList();

            PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ProvideStructureIdsPacket(structureIds));
        });
    }
}
