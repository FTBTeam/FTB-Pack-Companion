package dev.ftb.packcompanion.core.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record NameAndUuid(
    String name,
    UUID uuid
) {
    public static final StreamCodec<ByteBuf, NameAndUuid> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, NameAndUuid::name,
        UUIDUtil.STREAM_CODEC, NameAndUuid::uuid,
        NameAndUuid::new
    );
}
