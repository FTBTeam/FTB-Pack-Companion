package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

// From client to server, handled on the server
public record RequestStructurePacket(
        ResourceLocation structureId
) implements CustomPacketPayload {
    public static final Type<RequestStructurePacket> TYPE = new Type<>(PackCompanion.id("request_structure"));

    public static final StreamCodec<FriendlyByteBuf, RequestStructurePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, RequestStructurePacket::structureId,
        RequestStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestStructurePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var itemInHand = context.player().getMainHandItem();
            if (!(itemInHand.getItem() instanceof PlacerItem)) {
                return;
            }

            ServerLevel level = (ServerLevel) context.player().level();

            var structureCompound = level.getStructureManager().get(packet.structureId()).map(template -> {
                var compound = new CompoundTag();
                template.save(compound);
                return Optional.of(compound);
            }).orElse(Optional.empty());

            PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ProvideStructurePacket(structureCompound));
        });
    }
}
