package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NudgePacket(int x, int y, int z, boolean reset) implements CustomPacketPayload {
    public static final Type<NudgePacket> TYPE = new Type<>(PackCompanion.id("nudge"));
    public static final StreamCodec<ByteBuf, NudgePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, NudgePacket::x,
            ByteBufCodecs.INT, NudgePacket::y,
            ByteBufCodecs.INT, NudgePacket::z,
            ByteBufCodecs.BOOL, NudgePacket::reset,
            NudgePacket::new
    );

    public NudgePacket(int z, int y, int x) {
        this(x, y, z, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        var itemInHand = PlacerItem.getPlacerItemStack(context.player());
        if (itemInHand.isEmpty()) {
            return;
        }

        var existingNudgeOffset = PlacerItem.nudgeOffset(itemInHand.get())
                .orElse(BlockPos.ZERO);

        if (reset) {
            PlacerItem.setNudgeOffset(itemInHand.get(), BlockPos.ZERO);
        } else {
            PlacerItem.setNudgeOffset(itemInHand.get(), existingNudgeOffset.offset(x, y, z));
        }
    }
}
