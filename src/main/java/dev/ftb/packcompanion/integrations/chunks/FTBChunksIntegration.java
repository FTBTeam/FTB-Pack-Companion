package dev.ftb.packcompanion.integrations.chunks;

import dev.ftb.mods.ftbchunks.api.event.CustomMinYEvent;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.utils.CustomYConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.OptionalInt;

public class FTBChunksIntegration {
    public static void init() {
        CustomMinYEvent.REGISTER.register(registry -> registry.register((level, pos) -> {
            List<CustomYConfig> customYLevelChunkPositions = PCCommonConfig.CUSTOM_Y_LEVEL_CHUNK_POSITIONS.get();
            if (customYLevelChunkPositions.isEmpty()) {
                return OptionalInt.empty();
            }

            var currentDimension = level.dimension().location();
            int x = pos.getX();
            int z = pos.getZ();

            for (var customYConfig : customYLevelChunkPositions) {
                if (!matchesDimension(customYConfig.equalityCheck(), customYConfig.dimension(), currentDimension)) {
                    continue;
                }

                int range = customYConfig.range();
                int configX = customYConfig.x();
                int configZ = customYConfig.z();

                int dx = x - configX;
                int dz = z - configZ;

                // If we're doing it as a radius, we need to do a pythagorean check
                // If we're not, we do a square check
                if (customYConfig.asRadius()) {
                    // Compare squared distances — faster, no sqrt
                    if (dx * dx + dz * dz <= range * range) {
                        return OptionalInt.of(customYConfig.minY());
                    }
                } else {
                    // Axis-aligned square check
                    if (Math.abs(dx) <= range && Math.abs(dz) <= range) {
                        return OptionalInt.of(customYConfig.minY());
                    }
                }
            }

            return OptionalInt.empty();
        }));
    }

    private static boolean matchesDimension(CustomYConfig.DimensionEqualityCheck equalityCheck, String configDimension, ResourceLocation dimension) {
        var dimensionStr = dimension.toString();
        return switch (equalityCheck) {
            case STARTS_WITH -> dimensionStr.startsWith(configDimension);
            case EXACT_MATCH -> dimensionStr.equals(configDimension);
            case ENDS_WITH -> dimensionStr.endsWith(configDimension);
        };
    }
}
