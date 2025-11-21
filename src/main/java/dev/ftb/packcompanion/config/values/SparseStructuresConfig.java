package dev.ftb.packcompanion.config.values;

import java.util.ArrayList;
import java.util.List;

public record SparseStructuresConfig(
        boolean enabled,
        double globalSpreadFactor,
        List<CustomSpreadFactors> customSpreadFactors
) {
    public static final SparseStructuresConfig DEFAULT = new SparseStructuresConfig(false, 2D, new ArrayList<>());
}
