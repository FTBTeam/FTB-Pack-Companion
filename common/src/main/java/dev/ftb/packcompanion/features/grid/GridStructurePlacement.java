package dev.ftb.packcompanion.features.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

import dev.ftb.packcompanion.registry.StructurePlacementRegistry;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridStructurePlacement extends StructurePlacement {

    //Valid ints range to 2^24, so you can have singleton placements by setting spacing really high.
    public static final Codec<GridStructurePlacement> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance).and(instance.group(
                Codec.intRange(0, 16777216).fieldOf("spacing").forGetter(GridStructurePlacement::spacing),
                Codec.intRange(0, 16777216).fieldOf("x_offset").forGetter(GridStructurePlacement::xOffset),
                Codec.intRange(0, 16777216).fieldOf("z_offset").forGetter(GridStructurePlacement::zOffset))
            ).apply(instance, GridStructurePlacement::new)), GridStructurePlacement::validate).codec();

    private final int spacing;
    private final int xOffset;
    private final int zOffset;

    private static DataResult<GridStructurePlacement> validate(GridStructurePlacement placement) {
        return placement.spacing <= placement.xOffset || placement.spacing <= placement.zOffset
                ? DataResult.error(() -> "Spacing has to be larger than offsets")
                : DataResult.success(placement);
    }

    public GridStructurePlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<StructurePlacement.ExclusionZone> exclusionZone, int spacing, int xOffset, int zOffset) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    //Don't know what this does, but it's in Vanilla and some random mod probably needs it
    public GridStructurePlacement(int spacing, int xOffset, int zOffset, int salt) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty(), spacing, xOffset, zOffset);
    }

    public int spacing() {
        return this.spacing;
    }

    public int xOffset() {
        return this.xOffset;
    }

    public int zOffset() {
        return this.zOffset;
    }

    //Remember -1 = 9 mod 10 when placing near the origin.
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return Math.floorMod(x, this.spacing) == this.xOffset && Math.floorMod(z, this.spacing) == this.zOffset;
    }

    public @NotNull StructurePlacementType<?> type() {
        return StructurePlacementRegistry.GRID_PLACEMENT.get();
    }
}

