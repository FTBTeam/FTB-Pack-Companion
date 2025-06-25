package dev.ftb.packcompanion.features.spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class SpawnerBehaviourModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerBehaviourModifier.class);

    private final BaseSpawner baseSpawner;
    private int ticks = 0;

    public SpawnerBehaviourModifier(BaseSpawner baseSpawner) {
        this.baseSpawner = baseSpawner;
    }

    public void onTick(ServerLevel serverLevel, BlockPos blockPos) {
        if (this.ticks > 40) {
            this.attemptTorchDestroy(serverLevel, blockPos);
            this.ticks = 0;
        } else {
            this.ticks++;
        }
    }

    private void attemptTorchDestroy(ServerLevel serverLevel, BlockPos blockPos) {
        // Create a 5x5x4 area around the spawner
        AABB aabb = new AABB(blockPos).inflate(2.5, 3, 2.5);
        Stream<BlockPos> blockPosStream = BlockPos.betweenClosedStream(aabb);

        blockPosStream.forEach(pos -> {
            if (serverLevel.getBlockState(pos).getLightEmission() > 0) {
                serverLevel.destroyBlock(pos, true);
                // Spawn a fancy particle effect
                serverLevel.addParticle(ParticleTypes.FLAME, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
            }
        });
    }
}
