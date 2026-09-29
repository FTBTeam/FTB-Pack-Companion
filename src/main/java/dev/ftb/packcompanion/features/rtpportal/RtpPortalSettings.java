package dev.ftb.packcompanion.features.rtpportal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record RtpPortalSettings(Identifier sound) {
    public static final Codec<RtpPortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(RtpPortalSettings::sound)
    ).apply(instance, RtpPortalSettings::new));
}
