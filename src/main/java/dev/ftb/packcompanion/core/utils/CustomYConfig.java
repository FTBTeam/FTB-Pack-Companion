package dev.ftb.packcompanion.core.utils;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

public record CustomYConfig(
        String dimension,
        DimensionEqualityCheck equalityCheck,
        int x,
        int z,
        int range,
        int minY,
        boolean asRadius
) {
    public SNBTCompoundTag asCompound() {
        SNBTCompoundTag compound = new SNBTCompoundTag();
        compound.putString("dimension", dimension);
        compound.putString("equalityCheck", equalityCheck.toString());
        compound.putInt("x", x);
        compound.putInt("z", z);
        compound.putInt("minY", minY);
        compound.putInt("range", range);
        compound.putBoolean("asRadius", asRadius);
        return compound;
    }

    public static CustomYConfig fromCompound(SNBTCompoundTag compound) {
        String dimension = compound.getString("dimension");
        int x = compound.getInt("x");
        int z = compound.getInt("z");
        int range = compound.getInt("range");
        if (range <= 0) {
            throw new RuntimeException("Invalid range for custom_y_level_chunk_positions");
        }
        int minY = compound.getInt("minY");
        boolean asRadius = getOrDefault(compound, "asRadius", false);
        DimensionEqualityCheck equalityCheck = DimensionEqualityCheck.fromString(getOrDefault(compound, "equalityCheck", "exact_match"));
        return new CustomYConfig(dimension, equalityCheck, x, z, range, minY, asRadius);
    }

    public enum DimensionEqualityCheck {
        STARTS_WITH,
        EXACT_MATCH,
        ENDS_WITH;

        public static DimensionEqualityCheck fromString(String str) {
            try {
                return DimensionEqualityCheck.valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return STARTS_WITH;
            }
        }


        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private static <T> T getOrDefault(SNBTCompoundTag compound, String key, T defaultValue) {
        if (!compound.contains(key)) {
            return defaultValue;
        }

        Object value = compound.get(key);
        assert value != null;
        if (value.getClass().isInstance(defaultValue)) {
            return (T) value;
        }

        return defaultValue;
    }
}
