package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;

public class GameRuleMapping extends AbstractMapValue<Tag> {
    public GameRuleMapping(@Nullable SNBTConfig c, String n, Map<String, Tag> def) {
        super(c, n, def);
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
            public <T extends GameRules.Value<T>> void visit(GameRules.@NotNull Key<T> key, GameRules.@NotNull Type<T> type) {
                knownGameRuleKeys.add(key.getId());
            }
        });

        for (String key : keys) {
            if (!knownGameRuleKeys.contains(key)) {
                throw new IllegalStateException("Unknown game rule key in config: " + key);
            }
        }

        super.read(compoundTag);
    }

    @Override
    Tag readValue(Tag tag) {
        if (tag.getId() == Tag.TAG_BYTE || tag.getId() == Tag.TAG_INT) {
            return tag;
        } else {
            throw new IllegalStateException("Invalid tag type for game rule key '" + key + "': " + tag.getId());
        }
    }

    @Override
    Tag writeValue(Tag value) {
        return value;
    }
}
