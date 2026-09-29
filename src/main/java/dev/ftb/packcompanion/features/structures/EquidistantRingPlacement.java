package dev.ftb.packcompanion.features.structures;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Code originally by @Tazz
 * <p>
 * <a href="https://cdn.feed-the-beast.com/immutable/mod-perms/tazz-circle-structs-perm.png">consent link</a>
 */
public class EquidistantRingPlacement extends StructurePlacement {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile boolean hasLoggedPositions = false;

    public static final MapCodec<EquidistantRingPlacement> CODEC
            = RecordCodecBuilder.mapCodec(instance -> placementCodec(instance)
            .and(RingConfig.CODEC.forGetter(p -> p.config))
            .apply(instance, EquidistantRingPlacement::new)
    );

    private final RingConfig config;

    public EquidistantRingPlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone,
            RingConfig config
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);

        this.config = config;

        LOGGER.debug("Loaded '{}': salt={}, distance={}, count={}, center=({}, {}), totalStructures={}, structureIndex={}",
                config.label, salt, config.distance, config.count, config.centerX, config.centerZ, config.totalStructures, config.structureIndex);
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructuresFeature.EQUIDISTANT_RING_PLACEMENT.get();
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        long worldSeed = state.getLevelSeed();

        // Deterministic RNG seeded with world seed + placement salt
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(worldSeed, 0, salt());

        // Determine base rotation angle
        double initialAngle = config.fixedAngle >= 0.0f ?
                Math.toRadians(config.fixedAngle) :
                random.nextDouble() * 2 * Math.PI;

        boolean shouldLog = !this.hasLoggedPositions;
        if (shouldLog) {
            this.hasLoggedPositions = true;
            LOGGER.info("Computing ring '{}': salt={}, distance={}, count={}, center=({}, {}), totalStructures={}, structureIndex={}",
                    config.label, salt(), config.distance, config.count, config.centerX, config.centerZ, config.totalStructures, config.structureIndex);
        }

        // Evaluate each target node in the ring
        // Always compute ALL nodes to keep RNG state synchronized across round-robin placements
        for (int i = 0; i < config.count; i++) {
            double currentAngle = initialAngle + (i * (2.0 * Math.PI / config.count));

            // Apply distance variance
            int variedDistance = config.distance;
            if (config.distanceVariance > 0) {
                variedDistance += random.nextInt(config.distanceVariance * 2 + 1) - config.distanceVariance;
            }

            // Polar to Cartesian block coordinates
            int targetBlockX = config.centerX + (int) (variedDistance * Math.cos(currentAngle));
            int targetBlockZ = config.centerZ + (int) (variedDistance * Math.sin(currentAngle));

            // Block coordinates to chunk coordinates (>> 4 divides by 16)
            int targetChunkX = targetBlockX >> 4;
            int targetChunkZ = targetBlockZ >> 4;

            // Apply spread tolerance via deterministic sub-chunk scattering
            if (config.spreadTolerance > 0) {
                // Re-seed RNG based on the target chunk for deterministic scatter
                random.setLargeFeatureSeed(worldSeed, targetChunkX, targetChunkZ);
                int offsetX = random.nextInt(config.spreadTolerance * 2 + 1) - config.spreadTolerance;
                int offsetZ = random.nextInt(config.spreadTolerance * 2 + 1) - config.spreadTolerance;
                targetChunkX += offsetX;
                targetChunkZ += offsetZ;
            }

            boolean isMyNode = config.totalStructures <= 1 || (i % config.totalStructures) == config.structureIndex;

            if (shouldLog && isMyNode) {
                LOGGER.info("   '{}' Node {} -> block({}, ~, {}) chunk({}, {}) angle={}deg dist={}",
                        config.label, i, targetChunkX * 16 + 8, targetChunkZ * 16 + 8, targetChunkX, targetChunkZ,
                        String.format("%.1f", Math.toDegrees(currentAngle) % 360), variedDistance);
            }

            // Round-robin: only match nodes assigned to this structure index
            if (isMyNode && chunkX == targetChunkX && chunkZ == targetChunkZ) {
                return true;
            }
        }

        return false;
    }

    public record RingConfig(
            String label,
            int distance,
            int count,
            int spreadTolerance,
            int distanceVariance,
            int centerX,
            int centerZ,
            float fixedAngle,
            int totalStructures,
            int structureIndex
    ) {
        public static final MapCodec<RingConfig> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.optionalFieldOf("label", "unnamed").forGetter(RingConfig::label),
                Codec.INT.fieldOf("distance").forGetter(RingConfig::distance),
                Codec.INT.fieldOf("count").forGetter(RingConfig::count),
                Codec.INT.optionalFieldOf("spread_tolerance", 3).forGetter(RingConfig::spreadTolerance),
                Codec.INT.optionalFieldOf("distance_variance", 50).forGetter(RingConfig::distanceVariance),
                Codec.INT.optionalFieldOf("center_x", 0).forGetter(RingConfig::centerX),
                Codec.INT.optionalFieldOf("center_z", 0).forGetter(RingConfig::centerZ),
                Codec.FLOAT.optionalFieldOf("fixed_angle", -1f).forGetter(RingConfig::fixedAngle),
                Codec.INT.optionalFieldOf("total_structures", 1).forGetter(RingConfig::totalStructures),
                Codec.INT.optionalFieldOf("structure_index", 0).forGetter(RingConfig::structureIndex)
        ).apply(builder, RingConfig::new));
    }
}
