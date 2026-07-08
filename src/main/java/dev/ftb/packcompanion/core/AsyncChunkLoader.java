package dev.ftb.packcompanion.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AsyncChunkLoader {

    private AsyncChunkLoader() {
    }

    public static void whenReady(ServerLevel level, int centerChunkX, int centerChunkZ, int radius, Runnable callback) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<ChunkPos> toForce = new ArrayList<>();
        for (int cx = centerChunkX - radius; cx <= centerChunkX + radius; cx++) {
            for (int cz = centerChunkZ - radius; cz <= centerChunkZ + radius; cz++) {
                toForce.add(new ChunkPos(cx, cz));
                futures.add(level.getChunkSource().getChunkFuture(cx, cz, ChunkStatus.FULL, true));
            }
        }
        MinecraftServer server = level.getServer();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> server.execute(() -> {
                    for (ChunkPos pos : toForce) {
                        level.setChunkForced(pos.x, pos.z, true);
                    }
                    callback.run();
                }));
    }
}
