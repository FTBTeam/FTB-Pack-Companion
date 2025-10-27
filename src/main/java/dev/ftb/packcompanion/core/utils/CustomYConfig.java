package dev.ftb.packcompanion.core.utils;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.resources.ResourceLocation;

public record CustomYConfig(
        ResourceLocation dimension,
        int x,
        int z,
        int range,
        int minY,
        boolean asRadius
) {
    public SNBTCompoundTag asCompound() {
        SNBTCompoundTag compound = new SNBTCompoundTag();
        compound.putString("dimension", dimension.toString());
        compound.putInt("x", x);
        compound.putInt("z", z);
        compound.putInt("minY", minY);
        compound.putInt("range", range);
        compound.putBoolean("asRadius", asRadius);
        return compound;
    }

    public static CustomYConfig fromCompound(SNBTCompoundTag compound) {
        ResourceLocation dimension = ResourceLocation.tryParse(compound.getString("dimension"));
        int x = compound.getInt("x");
        int z = compound.getInt("z");
        int range = compound.getInt("range");
        int minY = compound.getInt("minY");
        boolean asRadius = compound.getBoolean("asRadius");
        return new CustomYConfig(dimension, x, z, range, minY, asRadius);
    }
}
