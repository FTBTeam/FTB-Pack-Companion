package dev.ftb.packcompanion.core.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MinMax(
        int min,
        int max
) {
    public static Codec<MinMax> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("min").forGetter(MinMax::min),
                    Codec.INT.fieldOf("max").forGetter(MinMax::max)
            ).apply(instance, MinMax::new)
    );

    public static StreamCodec<ByteBuf, MinMax> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MinMax::min,
            ByteBufCodecs.INT,
            MinMax::max,
            MinMax::new
    );

    public boolean isInRange(int value) {
        return value >= min && value <= max;
    }

    public static MinMax of(int min, int max) {
        return new MinMax(min, max);
    }

    public static MinMax exact(int value) {
        return new MinMax(value, value);
    }

    public boolean isExact() {
        return min == max;
    }
}
