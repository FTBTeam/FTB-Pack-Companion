package dev.ftb.packcompanion.features.kube.mods;

import dev.ftb.mods.ftbchunks.api.ClaimResult;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FTBChunks {
    public static final FTBChunks INSTANCE = new FTBChunks();

    public @Nullable ClaimedChunk chunkData(Level level, int x, int z) {
        return FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level.dimension(), x, z));
    }

    public boolean isClaimed(Level level, int x, int z) {
        return chunkData(level, x, z) != null;
    }

    public boolean isForceLoaded(Level level, int x, int z) {
        var chunk = chunkData(level, x, z);
        if (chunk == null) {
            return false;
        }

        return chunk.isForceLoaded();
    }

    public UUID teamIdForChunk(Level level, int x, int z) {
        var chunk = chunkData(level, x, z);
        if (chunk == null) {
            return null;
        }

        return chunk.getTeamData().getTeam().getId();
    }

    public UUID chunkOwner(Level level, int x, int z) {
        var chunk = chunkData(level, x, z);
        if (chunk == null) {
            return null;
        }

        return chunk.getTeamData().getTeam().getOwner();
    }

    public ClaimResult claimChunk(ServerPlayer player, Level level, int x, int z) {
        return FTBChunksAPI.api().claimAsPlayer(player, level.dimension(), new ChunkPos(x, z), false);
    }
}
