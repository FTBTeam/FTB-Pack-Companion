package dev.ftb.packcompanion.core.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import org.joml.Vector2f;

public class ExtraCodecs {
    public static final Codec<Vector2f> VECTOR2F_CODEC = Codec.pair(Codec.FLOAT, Codec.FLOAT).xmap(
            v -> new Vector2f(v.getFirst(), v.getSecond()),
            v -> new Pair<>(v.x, v.y)
    );
}
