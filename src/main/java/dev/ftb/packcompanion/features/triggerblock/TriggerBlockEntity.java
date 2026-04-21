package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TriggerBlockEntity extends BlockEntity {
    private String name = "unknown";
    private String ignorePlayersWithTag = null;

    public TriggerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TriggerBlockFeature.TRIGGER_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("name", name);
        if (ignorePlayersWithTag != null && !ignorePlayersWithTag.isEmpty()) {
            output.putString("ignoreTag", ignorePlayersWithTag);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("name").ifPresent(n -> this.name = n);
        input.getString("ignoreTag").ifPresent(t -> this.ignorePlayersWithTag = t);
    }

    public String name() {
        return name;
    }

    public String ignorePlayersWithTag() {
        return ignorePlayersWithTag;
    }
}
