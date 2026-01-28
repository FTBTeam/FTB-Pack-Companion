package dev.ftb.packcompanion.features.triggerblock;

import dev.ftb.packcompanion.core.utils.ClientHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerBlock extends Block implements EntityBlock {
    public TriggerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        TriggerBlockController.getInstance(player.level().isClientSide()).onPlayerIn(player, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof Level l && l.isClientSide()) {
            // This should be safe!
            if (ClientHelpers.clientIsSurvival()) {
                return Shapes.empty();
            } else {
                return Shapes.block();
            }
        }

        return Shapes.empty();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);

        if (level.getBlockEntity(pos) instanceof TriggerBlockEntity be) {
            CompoundTag tag = stack.getOrCreateTag();

            CompoundTag subtag = tag.contains(BlockItem.BLOCK_ENTITY_TAG) ? tag.getCompound(BlockItem.BLOCK_ENTITY_TAG) : new CompoundTag();
            subtag.putString("name", be.name());
            tag.put(BlockItem.BLOCK_ENTITY_TAG, subtag);
        }

        return stack;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TriggerBlockEntity(blockPos, blockState);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag()) {
            CompoundTag nbtData = stack.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
            if (nbtData != null) {
                tooltip.add(Component.literal("Name: ").append(nbtData.getString("name")).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
