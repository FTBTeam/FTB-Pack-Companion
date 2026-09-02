package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RotatePlacerPacket(boolean previous) implements CustomPacketPayload {
    public static final Type<RotatePlacerPacket> TYPE = new Type<>(PackCompanion.id("rotate_placer"));
    public static final StreamCodec<ByteBuf, RotatePlacerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RotatePlacerPacket::previous,
            RotatePlacerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        var itemInHand = PlacerItem.getPlacerItemStack(context.player());
        if (itemInHand.isEmpty()) {
            return;
        }

        var currentRotation = PlacerItem.rotation(itemInHand.get()).orElse(Rotation.NONE);
        var ordinalOfRotation = currentRotation.ordinal();
        // Select the previous or next rotation in the enum, wrapping around if necessary
        var newOrdinal = previous ? (ordinalOfRotation - 1 + Rotation.values().length) % Rotation.values().length : (ordinalOfRotation + 1) % Rotation.values().length;

        var newRotation = Rotation.values()[newOrdinal];
        PlacerItem.setRotation(itemInHand.get(), newRotation);
        context.player().displayClientMessage(Component.translatable("ftbpackcompanion.structureplacer.rotated"), true);
    }
}
