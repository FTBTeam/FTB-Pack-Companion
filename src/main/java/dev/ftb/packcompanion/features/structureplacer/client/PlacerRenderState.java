package dev.ftb.packcompanion.features.structureplacer.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Eases the structure-placer preview's rendered position and rotation and contains the
 * internal state of the animation.
 */
public class PlacerRenderState {
    private static final double HALF_LIFE_SECONDS = 0.08;
    private static final double ROTATION_HALF_LIFE_SECONDS = 0.12;
    private static final long STALE_THRESHOLD_NANOS = 250_000_000L;

    private ResourceLocation structureId;
    private Vec3 position;
    private float angleDegrees;
    private long lastUpdateNanos = -1;

    public Pose next(ResourceLocation structureId, BlockPos targetPos, float targetAngleDegrees) {
        long now = System.nanoTime();
        var target = Vec3.atLowerCornerOf(targetPos);

        boolean reset = position == null || !structureId.equals(this.structureId) || isStale(now);

        if (reset) {
            position = target;
            angleDegrees = targetAngleDegrees;
        } else {
            double dt = (now - lastUpdateNanos) / 1_000_000_000.0;
            position = position.lerp(target, smoothingFactor(dt, HALF_LIFE_SECONDS));
            angleDegrees = (float) (angleDegrees + Mth.wrapDegrees(targetAngleDegrees - angleDegrees) * smoothingFactor(dt, ROTATION_HALF_LIFE_SECONDS));
        }

        this.structureId = structureId;
        lastUpdateNanos = now;

        return new Pose(position, angleDegrees);
    }

    private boolean isStale(long now) {
        return lastUpdateNanos < 0 || (now - lastUpdateNanos) > STALE_THRESHOLD_NANOS;
    }

    private static double smoothingFactor(double dtSeconds, double halfLifeSeconds) {
        return 1.0 - Math.pow(0.5, dtSeconds / halfLifeSeconds);
    }

    public record Pose(Vec3 position, float angleDegrees) {}
}
