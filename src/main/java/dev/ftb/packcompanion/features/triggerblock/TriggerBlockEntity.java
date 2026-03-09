package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriggerBlockEntity extends BlockEntity {
    private String name = "unknown";
    private String ignorePlayersWithTag = null;

    public TriggerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TriggerBlockFeature.TRIGGER_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) handleUpdateTag(pkt.getTag());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("name", name);
        if (ignorePlayersWithTag != null && !ignorePlayersWithTag.isEmpty()) {
            tag.putString("ignoreTag", ignorePlayersWithTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadAdditional(tag);
    }

    protected void loadAdditional(CompoundTag tag) {
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
