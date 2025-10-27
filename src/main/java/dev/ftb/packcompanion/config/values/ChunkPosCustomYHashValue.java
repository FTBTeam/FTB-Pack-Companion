package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChunkPosCustomYHashValue extends BaseValue<Map<Integer, Set<ChunkPos>>> {
    private final HashMap<Long, Integer> lookup = new HashMap<>();

    public ChunkPosCustomYHashValue(@Nullable SNBTConfig c, String n, Map<Integer, Set<ChunkPos>> def) {
        super(c, n, def);
        super.set(new HashMap<>());
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        var compound = new SNBTCompoundTag();

        for (Map.Entry<Integer, Set<ChunkPos>> pos : get().entrySet()) {
            var listTag = new ListTag();
            for (ChunkPos chunkPos : pos.getValue()) {
                var entryTag = new SNBTCompoundTag();
                entryTag.putInt("x", chunkPos.x);
                entryTag.putInt("z", chunkPos.z);
                listTag.add(entryTag);
            }

            compound.put(String.valueOf(pos.getKey()), listTag);
        }

        tag.put(key, compound);
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        var compound = tag.getCompound(key);
        Map<Integer, Set<ChunkPos>> map = new HashMap<>();

        for (String k : compound.getAllKeys()) {
            int minY = Integer.parseInt(k);
            var listTag = compound.getList(k, Tag.TAG_COMPOUND);
            Set<ChunkPos> positions = new HashSet<>();
            for (Tag value : listTag) {
                var entryTag = (SNBTCompoundTag) value;
                int x = entryTag.getInt("x");
                int z = entryTag.getInt("z");
                positions.add(new ChunkPos(x, z));
            }
            map.put(minY, positions);
        }

        set(map);
    }

    @Override
    public void set(Map<Integer, Set<ChunkPos>> value) {
        super.set(value);

        lookup.clear();
        for (Map.Entry<Integer, Set<ChunkPos>> entry : value.entrySet()) {
            int minY = entry.getKey();
            for (ChunkPos pos : entry.getValue()) {
                lookup.put(ChunkPos.asLong(pos.x, pos.z), minY);
            }
        }
    }

    public Map<Long, Integer> lookup() {
        return Collections.unmodifiableMap(lookup);
    }
}
