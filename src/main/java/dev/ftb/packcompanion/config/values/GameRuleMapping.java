package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GameRuleMapping extends BaseValue<Map<String, Tag>> {
    public GameRuleMapping(@Nullable SNBTConfig c, String n, Map<String, Tag> def) {
        super(c, n, def);
    }

    @Override
    public void write(SNBTCompoundTag compoundTag) {
        SNBTCompoundTag tag = new SNBTCompoundTag();

        for (Map.Entry<String, Tag> entry : get().entrySet()) {
            tag.put(entry.getKey(), entry.getValue());
        }

        compoundTag.comment(this.key, "Mapping of game rule keys to their values. Boolean game rules are stored as byte tags (0 or 1), integer game rules are stored as int tags.");
        compoundTag.put(this.key, tag);
    }

    @Override
    public void read(SNBTCompoundTag compoundTag) {
        if (!compoundTag.contains(this.key)) {
            set(Map.of());
            return;
        }

        var tag = compoundTag.getCompound(this.key);
        var keys = tag.getAllKeys();

        var knownGameRuleKeys = new HashSet<String>();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(@Nonnull GameRules.Key<T> key, @Nonnull GameRules.Type<T> type) {
                knownGameRuleKeys.add(key.getId());
            }
        });

        for (String key : keys) {
            if (!knownGameRuleKeys.contains(key)) {
                throw new IllegalStateException("Unknown game rule key in config: " + key);
            }
        }

        Map<String, Tag> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            Tag valueTag = tag.get(key);
            if (valueTag == null) {
                continue;
            }

            if (valueTag.getId() == Tag.TAG_BYTE) {
                map.put(key, valueTag);
            } else if (valueTag.getId() == Tag.TAG_INT) {
                map.put(key, valueTag);
            } else {
                throw new IllegalStateException("Invalid tag type for game rule key '" + key + "': " + valueTag.getId());
            }
        }

        set(map);
    }
}
