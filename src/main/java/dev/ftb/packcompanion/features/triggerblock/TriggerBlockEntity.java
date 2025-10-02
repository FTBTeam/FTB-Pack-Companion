package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriggerBlockEntity extends BlockEntity {
    private String name = "unknown";

    public TriggerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TriggerBlockFeature.TRIGGER_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("name", name);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("name")) {
            name = tag.getString("name");
        }
    }

    public String name() {
        return name;
    }
}
