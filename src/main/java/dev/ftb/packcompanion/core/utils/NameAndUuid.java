package dev.ftb.packcompanion.core.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record NameAndUuid(
    String name,
    UUID uuid
) {
    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeUUID(uuid);
    }

    public static NameAndUuid fromBuffer(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        UUID uuid = buf.readUUID();
        return new NameAndUuid(name, uuid);
    }
}
