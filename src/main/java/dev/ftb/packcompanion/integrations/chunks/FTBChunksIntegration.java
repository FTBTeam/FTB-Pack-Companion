package dev.ftb.packcompanion.integrations.chunks;

import dev.ftb.mods.ftbchunks.api.event.CustomMinYEvent;
import dev.ftb.packcompanion.config.PCCommonConfig;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.OptionalInt;

public class FTBChunksIntegration {
    public static void init() {
        CustomMinYEvent.REGISTER.register(registry -> registry.register((level, pos) -> {
            Map<Long, Integer> customYLevelChunkPositions = PCCommonConfig.CUSTOM_Y_LEVEL_CHUNK_POSITIONS.lookup();
            if (customYLevelChunkPositions.isEmpty()) {
                return OptionalInt.empty();
            }

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            Long chunkKey = ChunkPos.asLong(chunkX, chunkZ);

            if (customYLevelChunkPositions.containsKey(chunkKey)) {
                return OptionalInt.of(customYLevelChunkPositions.get(chunkKey));
            }

            return OptionalInt.empty();
        }));
    }
}
