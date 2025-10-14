package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriggerBlockEntity extends BlockEntity {
    private String name = "unknown";
    private String ignorePlayersWithTag = null;

    public TriggerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TriggerBlockFeature.TRIGGER_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("name", name);
        if (ignorePlayersWithTag != null && !ignorePlayersWithTag.isEmpty()) {
            tag.putString("ignoreTag", ignorePlayersWithTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("name")) {
            name = tag.getString("name");
        }
        if (tag.contains("ignoreTag")) {
            ignorePlayersWithTag = tag.getString("ignoreTag");
        }
    }

    public String name() {
        return name;
    }

    public String ignorePlayersWithTag() {
        return ignorePlayersWithTag;
    }
}
