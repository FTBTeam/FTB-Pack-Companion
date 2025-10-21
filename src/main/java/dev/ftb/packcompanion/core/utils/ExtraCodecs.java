package dev.ftb.packcompanion.core.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector2f;

public class ExtraCodecs {
    public static final Codec<Vector2f> VECTOR2F_CODEC = Codec.pair(Codec.FLOAT, Codec.FLOAT).xmap(
            v -> new Vector2f(v.getFirst(), v.getSecond()),
            v -> new Pair<>(v.x, v.y)
    );

    public static final StreamCodec<ByteBuf, Vector2f> VECTOR2F_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, v -> v.x,
            ByteBufCodecs.FLOAT, v -> v.y,
            Vector2f::new
    );
}
