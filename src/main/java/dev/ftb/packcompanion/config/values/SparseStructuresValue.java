package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class SparseStructuresValue extends BaseValue<SparseStructuresConfig> {
    public SparseStructuresValue(@Nullable SNBTConfig c, String n, SparseStructuresConfig def) {
        super(c, n, def);
    }

    @Override
    public void write(SNBTCompoundTag compoundTag) {
        SNBTCompoundTag tag = new SNBTCompoundTag();

        tag.comment("enabled", "Whether sparse structures are enabled.");
        tag.putBoolean("enabled", get().enabled());

        tag.comment("global_spread_factor", "The global spread factor for all structures when no custom spread factor is defined.");
        tag.putDouble("global_spread_factor", get().globalSpreadFactor());

        var list = new ListTag();
        for (var custom : get().customSpreadFactors()) {
            var customTag = new SNBTCompoundTag();
            customTag.putString("structure", custom.structure());
            customTag.putDouble("spread_factor", custom.spreadFactor());
            list.add(customTag);
        }

        tag.comment("custom_spread_factors", "Custom spread factors for specific structures.");
        tag.put("custom_spread_factors", list);

        compoundTag.comment(this.key, "Sparse structures configuration. See https://github.com/MCTeamPotato/SparseStructuresReforged/tree/1201 for more information.");
        compoundTag.put(this.key, tag);
    }

    @Override
    public void read(SNBTCompoundTag parent) {
        if (!parent.contains(this.key)) {
            set(SparseStructuresConfig.DEFAULT);
            return;
        }

        var tag = (SNBTCompoundTag) parent.get(this.key);
        if (tag == null) {
            set(SparseStructuresConfig.DEFAULT);
            return;
        }

        var enabled = tag.getBoolean("enabled");
        var globalSpreadFactor = tag.getDouble("global_spread_factor");
        var customSpreadFactors = new ArrayList<CustomSpreadFactors>();

        if (tag.contains("custom_spread_factors")) {
            var listTag = tag.getList("custom_spread_factors", Tag.TAG_COMPOUND);
            for (var customTag : listTag) {
                var compound = (SNBTCompoundTag) customTag;
                customSpreadFactors.add(new CustomSpreadFactors(
                        compound.getString("structure"),
                        compound.getDouble("spread_factor")
                ));
            }
        }

        set(new SparseStructuresConfig(enabled, globalSpreadFactor, customSpreadFactors));
    }
}
