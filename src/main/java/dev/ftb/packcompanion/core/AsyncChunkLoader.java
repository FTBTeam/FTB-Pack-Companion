package dev.ftb.packcompanion.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public final class AsyncChunkLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncChunkLoader.class);

    private static final int TIMEOUT_TICKS = 6000;

    private static final int TICKET_DISTANCE = 2;

    private static final TicketType<ChunkPos> ASYNC_LOAD =
            TicketType.create("ftbpc_async_load", Comparator.comparingLong(ChunkPos::toLong));

    private static final Deque<PendingLoad> PENDING = new ArrayDeque<>();

    private static boolean listenerRegistered = false;

    private AsyncChunkLoader() {
    }

    public static void whenReady(ServerLevel level, int centerChunkX, int centerChunkZ, int radius, Runnable callback) {
        ensureListener();

        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = centerChunkX - radius; cx <= centerChunkX + radius; cx++) {
            for (int cz = centerChunkZ - radius; cz <= centerChunkZ + radius; cz++) {
                ChunkPos pos = new ChunkPos(cx, cz);
                chunks.add(pos);
                level.getChunkSource().addRegionTicket(ASYNC_LOAD, pos, TICKET_DISTANCE, pos);
            }
        }

        PENDING.add(new PendingLoad(level, chunks, callback));
    }

    private static void ensureListener() {
        if (!listenerRegistered) {
            listenerRegistered = true;
            MinecraftForge.EVENT_BUS.addListener(AsyncChunkLoader::onServerTick);
        }
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }

        List<PendingLoad> ready = new ArrayList<>();
        Iterator<PendingLoad> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingLoad job = it.next();
            job.age++;

            boolean done;
            try {
                done = job.isReady();
            } catch (Exception e) {
                LOGGER.error("AsyncChunkLoader readiness check failed; dropping job", e);
                it.remove();
                job.releaseTickets();
                continue;
            }

            if (done || job.age > TIMEOUT_TICKS) {
                if (!done) {
                    LOGGER.warn("AsyncChunkLoader gave up after {} ticks waiting for {} chunk(s) in {}",
                            TIMEOUT_TICKS, job.chunks.size(), job.level.dimension().location());
                }
                it.remove();
                ready.add(job);
            }
        }

        for (PendingLoad job : ready) {
            try {
                job.applyForced();
                job.callback.run();
            } catch (Throwable t) {
                LOGGER.error("AsyncChunkLoader callback failed", t);
            } finally {
                job.releaseTickets();
            }
        }
    }

    private static final class PendingLoad {
        private final ServerLevel level;
        private final List<ChunkPos> chunks;
        private final Runnable callback;
        private int age;

        private PendingLoad(ServerLevel level, List<ChunkPos> chunks, Runnable callback) {
            this.level = level;
            this.chunks = chunks;
            this.callback = callback;
        }

        private boolean isReady() {
            for (ChunkPos pos : chunks) {
                if (!level.hasChunk(pos.x, pos.z)) {
                    return false;
                }
            }
            return true;
        }

        private void applyForced() {
            for (ChunkPos pos : chunks) {
                if (level.hasChunk(pos.x, pos.z)) {
                    level.setChunkForced(pos.x, pos.z, true);
                }
            }
        }

        private void releaseTickets() {
            try {
                for (ChunkPos pos : chunks) {
                    level.getChunkSource().removeRegionTicket(ASYNC_LOAD, pos, TICKET_DISTANCE, pos);
                }
            } catch (Throwable t) {
                LOGGER.error("AsyncChunkLoader failed to release chunk tickets", t);
            }
        }
    }
}
