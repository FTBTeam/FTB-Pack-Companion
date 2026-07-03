package dev.ftb.packcompanion.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AsyncChunkLoader {

    private AsyncChunkLoader() {
    }

    public static void whenReady(ServerLevel level, int centerChunkX, int centerChunkZ, int radius, Runnable callback) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int cx = centerChunkX - radius; cx <= centerChunkX + radius; cx++) {
            for (int cz = centerChunkZ - radius; cz <= centerChunkZ + radius; cz++) {
                level.setChunkForced(cx, cz, true);
                futures.add(level.getChunkSource().getChunkFuture(cx, cz, ChunkStatus.FULL, true));
            }
        }
        MinecraftServer server = level.getServer();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> server.execute(callback));
    }
}
