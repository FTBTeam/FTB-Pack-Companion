package dev.ftb.packcompanion.core.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record NameAndUuid(
    String name,
    UUID uuid
) {
    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static NameAndUuid fromBuffer(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        UUID uuid = new UUID(buf.readLong(), buf.readLong());
        return new NameAndUuid(name, uuid);
    }
}
