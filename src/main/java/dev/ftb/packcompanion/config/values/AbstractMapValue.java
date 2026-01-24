package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMapValue<T> extends BaseValue<Map<String, T>> {
    protected AbstractMapValue(@Nullable SNBTConfig config, String key, Map<String, T> defaultValue) {
        super(config, key, defaultValue);
    }

    @Override
    public void write(SNBTCompoundTag snbtCompoundTag) {
        var mapCompoundTag = new SNBTCompoundTag();
        for (var entry : get().entrySet()) {
            mapCompoundTag.put(entry.getKey(), writeValue(entry.getValue()));
        }

        if (this.comment != null) {
            snbtCompoundTag.comment(this.key, this.comment.toArray(new String[0]));
        }
        snbtCompoundTag.put(this.key, mapCompoundTag);
    }

    @Override
    public void read(SNBTCompoundTag snbtCompoundTag) {
        if (!snbtCompoundTag.contains(this.key)) {
            set(Map.of());
            return;
        }

        var mapCompoundTag = snbtCompoundTag.getCompound(this.key);
        var keys = mapCompoundTag.getAllKeys();

        var map = new HashMap<String, T>();
        for (var key : keys) {
            T value = readValue(mapCompoundTag.get(key));
            if (value != null) {
                map.put(key, value);
            }
        }

        set(map);
    }

    abstract T readValue(Tag tag);
    abstract Tag writeValue(T value);

    public static class CodecBased<T> extends AbstractMapValue<T> {
        private final Codec<T> codec;

        public CodecBased(@Nullable SNBTConfig config, String key, Map<String, T> defaultValue, Codec<T> codec) {
            super(config, key, defaultValue);
            this.codec = codec;
        }

        @Override
        T readValue(Tag tag) {
            return this.codec.decode(NbtOps.INSTANCE, tag).getOrThrow(s -> {
                throw new IllegalStateException("Failed to decode value for key '" + key + "': " + s);
            }).getFirst();
        }

        @Override
        Tag writeValue(T value) {
            return this.codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(s -> {
                throw new IllegalStateException("Failed to encode value for key '" + key + "': " + s);
            });
        }
    }
}
