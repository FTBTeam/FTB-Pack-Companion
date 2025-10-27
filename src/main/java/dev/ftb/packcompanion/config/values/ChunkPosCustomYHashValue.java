package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.packcompanion.core.utils.CustomYConfig;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChunkPosCustomYHashValue extends BaseValue<List<CustomYConfig>> {
    public ChunkPosCustomYHashValue(@Nullable SNBTConfig c, String n, List<CustomYConfig> def) {
        super(c, n, def);
        super.set(new ArrayList<>());
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        var list = new ListTag();

        for (CustomYConfig config : get()) {
            list.add(config.asCompound());
        }

        tag.put(key, list);
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        List<CustomYConfig> values = new ArrayList<>();
        var list = tag.getList(key, Tag.TAG_COMPOUND);

        for (var element : list) {
            var compound = (SNBTCompoundTag) element;
            values.add(CustomYConfig.fromCompound(compound));
        }

        set(values);
    }
}
