package dev.ftb.packcompanion.core.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CustomYConfig(
        String dimension,
        DimensionEqualityCheck equalityCheck,
        int x,
        int z,
        int range,
        int minY,
        boolean asRadius
) {
    public static final Codec<CustomYConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(CustomYConfig::dimension),
            // Use enum codec type for the codec
            Codec.STRING.xmap(DimensionEqualityCheck::valueOf, DimensionEqualityCheck::name).fieldOf("equalityCheck").forGetter(CustomYConfig::equalityCheck),
            Codec.INT.fieldOf("x").forGetter(CustomYConfig::x),
            Codec.INT.fieldOf("z").forGetter(CustomYConfig::z),
            Codec.INT.fieldOf("range").forGetter(CustomYConfig::range),
            Codec.INT.fieldOf("minY").forGetter(CustomYConfig::minY),
            Codec.BOOL.optionalFieldOf("asRadius", false).forGetter(CustomYConfig::asRadius)
    ).apply(instance, CustomYConfig::new));

    public enum DimensionEqualityCheck {
        STARTS_WITH,
        EXACT_MATCH,
        ENDS_WITH
    }
}
