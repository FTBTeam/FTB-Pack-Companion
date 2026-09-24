package dev.ftb.packcompanion.features.kube;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.UUID;

public class Utils {
    public static final UUID NIL_UUID = new UUID(0, 0);
    public static BlockPos ZERO_BLOCKPOS = new BlockPos(0, 0, 0);
    public static Vector3f ZERO_VECTOR3F = new Vector3f(0, 0, 0);
    public static Vector3d ZERO_VECTOR3D = new Vector3d(0, 0, 0);
    public static Vector3i ZERO_VECTOR3I = new Vector3i(0, 0, 0);
    public static AABB ZERO_AABB = new AABB(0, 0, 0, 0, 0, 0);

    public UUID randomUUID() {
        return UUID.randomUUID();
    }

    public String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public Vector3i vec3(Number x, Number y, Number z) {
        if (x == null || y == null || z == null) {
            return ZERO_VECTOR3I;
        }

        return new Vector3i(x.intValue(), y.intValue(), z.intValue());
    }

    public Vector3d vec3d(Number x, Number y, Number z) {
        if (x == null || y == null || z == null) {
            return ZERO_VECTOR3D;
        }

        return new Vector3d(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public Vector3f vec3f(Number x, Number y, Number z) {
        if (x == null || y == null || z == null) {
            return ZERO_VECTOR3F;
        }

        return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
    }

    public BlockPos blockPos(Number x, Number y, Number z) {
        if (x == null || y == null || z == null) {
            return ZERO_BLOCKPOS;
        }

        return new BlockPos(x.intValue(), y.intValue(), z.intValue());
    }

    public AABB boundingFromCorners(BlockPos min, BlockPos max) {
        if (min == null || max == null) {
            return ZERO_AABB;
        }

        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public AABB boundingSized(BlockPos pos, Number size) {
        if (pos == null || size == null) {
            return ZERO_AABB;
        }

        double s = size.doubleValue();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + s, pos.getY() + s, pos.getZ() + s);
    }

    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    public boolean isRealPlayer(Player player) {
        return !(player instanceof FakePlayer);
    }

    public boolean isOp(Player player) {
        return player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
    }
}
