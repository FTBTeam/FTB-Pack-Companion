package dev.ftb.packcompanion.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface PCCommonConfig {
    SNBTConfig CONFIG = SNBTConfig.create(PackCompanion.MOD_ID + "-common");

    BooleanValue BREAK_LIGHT_SOURCES_NEAR_SPAWNERS = CONFIG.addBoolean("break_light_sources_near_spawners", false)
            .comment("When enabled, light sources near spawners will be broken to allow for mob spawning.");

    BooleanValue IGNORE_LIGHT_LEVEL_FOR_SPAWNERS = CONFIG.addBoolean("ignore_light_level_for_spawners", false)
            .comment("When enabled, the light level around spawners will be ignored for mob spawning.");

    BooleanValue REMOVE_CONCENTRIC_RING_PLACEMENT_BIAS = CONFIG.addBoolean("remove_concentric_ring_placement_bias", false)
            .comment("When enabled, the random bias applied to concentric ring distances is removed");

    SparseStructuresValue SPARSE_STRUCTURES = CONFIG.add(new SparseStructuresValue(CONFIG, "sparse_structures", SparseStructuresConfig.DEFAULT));

    static void load() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".common", true);
    }

    class SparseStructuresValue extends BaseValue<SparseStructuresConfig> {
        protected SparseStructuresValue(@Nullable SNBTConfig c, String n, SparseStructuresConfig def) {
            super(c, n, def);
        }

        @Override
        public void write(SNBTCompoundTag compoundTag) {
            SNBTCompoundTag tag = new SNBTCompoundTag();

            tag.comment("enabled", "Whether sparse structures are enabled.");
            tag.putBoolean("enabled", get().enabled);

            tag.comment("global_spread_factor", "The global spread factor for all structures when no custom spread factor is defined.");
            tag.putDouble("global_spread_factor", get().globalSpreadFactor);

            var list = new ListTag();
            for (var custom : get().customSpreadFactors) {
                var customTag = new SNBTCompoundTag();
                customTag.putString("structure", custom.structure);
                customTag.putDouble("spread_factor", custom.spreadFactor);
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

    record SparseStructuresConfig(
            boolean enabled,
            double globalSpreadFactor,
            List<CustomSpreadFactors> customSpreadFactors
    ) {
        public static final SparseStructuresConfig DEFAULT = new SparseStructuresConfig(false, 2D, new ArrayList<>());
    }

    record CustomSpreadFactors(
            String structure,
            double spreadFactor
    ) {}
}
