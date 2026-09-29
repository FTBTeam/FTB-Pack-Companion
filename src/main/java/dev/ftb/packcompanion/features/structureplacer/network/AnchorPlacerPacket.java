package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record AnchorPlacerPacket() implements CustomPacketPayload {
    public static final Type<AnchorPlacerPacket> TYPE = new Type<>(PackCompanion.id("anchor_placer"));
    public static final AnchorPlacerPacket INSTANCE = new AnchorPlacerPacket();

    public static final StreamCodec<ByteBuf, AnchorPlacerPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        var itemInHand = PlacerItem.getPlacerItemStack(context.player());
        if (itemInHand.isEmpty()) {
            return;
        }

        Optional<BlockPos> anchorPos = PlacerItem.anchorPos(itemInHand.get());
        if (anchorPos.isEmpty()) {
            var blockPos = PlacerItem.blockPosFromPick(context.player());
            PlacerItem.setAnchorPos(itemInHand.get(), blockPos);
            context.player().sendOverlayMessage(Component.translatable("ftbpackcompanion.structureplacer.anchored"));
        } else {
            PlacerItem.setAnchorPos(itemInHand.get(), null);
            context.player().sendOverlayMessage(Component.translatable("ftbpackcompanion.structureplacer.released"));
        }
    }
}
