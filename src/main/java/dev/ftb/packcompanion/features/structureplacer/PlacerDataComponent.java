package dev.ftb.packcompanion.features.structureplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

import java.util.Optional;

public record PlacerDataComponent(
        Identifier structureId,
        Optional<BlockPos> anchorPos,
        Optional<Rotation> rotation,
        Optional<BlockPos> nudgeOffset
) {
    public static final PlacerDataComponent EMPTY = new PlacerDataComponent(null, Optional.empty(), Optional.empty(), Optional.empty());

    private static final StreamCodec<ByteBuf, Rotation> ROTATION_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Enum::ordinal,
            (ordinal) -> {
                for (Rotation rotation : Rotation.values()) {
                    if (rotation.ordinal() == ordinal) {
                        return rotation;
                    }
                }

                throw new IllegalArgumentException("Invalid ordinal for Rotation: " + ordinal);
            }
    );

    public PlacerDataComponent withStructureId(Identifier structureId) {
        return new PlacerDataComponent(structureId, anchorPos, rotation, nudgeOffset);
    }

    public PlacerDataComponent withAnchorPos(BlockPos anchorPos) {
        return new PlacerDataComponent(structureId, Optional.ofNullable(anchorPos), rotation, nudgeOffset);
    }

    public PlacerDataComponent withRotation(Rotation rotation) {
        return new PlacerDataComponent(structureId, anchorPos, Optional.of(rotation), nudgeOffset);
    }

    public PlacerDataComponent withNudgeOffset(BlockPos nudgeOffset) {
        return new PlacerDataComponent(structureId, anchorPos, rotation, Optional.of(nudgeOffset));
    }

    public static final Codec<PlacerDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("structure_id").forGetter(PlacerDataComponent::structureId),
            BlockPos.CODEC.optionalFieldOf("anchor_pos").forGetter(PlacerDataComponent::anchorPos),
            Rotation.CODEC.optionalFieldOf("rotation").forGetter(PlacerDataComponent::rotation),
            BlockPos.CODEC.optionalFieldOf("nudge_offset").forGetter(PlacerDataComponent::nudgeOffset)
    ).apply(instance, PlacerDataComponent::new));

    public static final StreamCodec<ByteBuf, PlacerDataComponent> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, PlacerDataComponent::structureId,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), PlacerDataComponent::anchorPos,
            ByteBufCodecs.optional(ROTATION_STREAM_CODEC), PlacerDataComponent::rotation,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), PlacerDataComponent::nudgeOffset,
            PlacerDataComponent::new
    );
}
