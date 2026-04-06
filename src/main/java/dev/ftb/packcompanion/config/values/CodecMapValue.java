package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.config.value.AbstractMapValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.util.Map;

public class CodecMapValue<T> extends AbstractMapValue<T> {
    public CodecMapValue(@Nullable Config config, String key, Map<String, T> defaultValue, Codec<T> codec) {
        super(config, key, defaultValue, codec);
    }

//    @Override
//    T readValue(Tag tag) {
//        return this.codec.decode(NbtOps.INSTANCE, tag).getOrThrow(s -> {
//            throw new IllegalStateException("Failed to decode value for key '" + key + "': " + s);
//        }).getFirst();
//    }
//
//    @Override
//    Tag writeValue(T value) {
//        return this.codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(s -> {
//            throw new IllegalStateException("Failed to encode value for key '" + key + "': " + s);
//        });
//    }
}
