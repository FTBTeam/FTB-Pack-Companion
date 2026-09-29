package dev.ftb.packcompanion.features.rtpportal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RtpPortalBlock extends Block {
    public static final MapCodec<RtpPortalBlock> CODEC = simpleCodec(RtpPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final long COOLDOWN_TICKS = 100L;

    private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Block.column(4.0, 16.0, 0.0, 16.0));
    private static final Map<UUID, PlayerState> PLAYER_STATES = new ConcurrentHashMap<>();

    public RtpPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    public static void clearPlayerState(UUID playerId) {
        PLAYER_STATES.remove(playerId);
    }

    @Override
    protected MapCodec<RtpPortalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(AXIS));
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return state.getShape(level, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        long now = level.getGameTime();
        PlayerState previous = PLAYER_STATES.get(player.getUUID());
        boolean stillInside = previous != null && now - previous.lastInside() <= 1;
        boolean coolingDown = previous != null && now < previous.readyAt();

        if (stillInside || coolingDown) {
            PLAYER_STATES.put(player.getUUID(), new PlayerState(now, previous.readyAt()));
            return;
        }

        PLAYER_STATES.put(player.getUUID(), new PlayerState(now, now + COOLDOWN_TICKS));

        if (!RtpPortalFeature.ENABLED) {
            return;
        }

        RtpPortalFeature.playTriggerSound(level, pos);
        level.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "rtp");
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getHorizontalDirection().getAxis();
        axis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

        return defaultBlockState().setValue(AXIS, axis);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
            default -> state;
        };
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(RtpPortalFeature.RTP_PORTAL_ITEM.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    RtpPortalFeature.RTP_PORTAL_SOUND.get(), SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        boolean spansX = state.getValue(AXIS) == Direction.Axis.X;
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double xo = (random.nextFloat() - 0.5D) * 0.5D;
            double yo = (random.nextFloat() - 0.5D) * 0.5D;
            double zo = (random.nextFloat() - 0.5D) * 0.5D;
            int k = random.nextInt(2) * 2 - 1;
            if (spansX) {
                z = pos.getZ() + 0.5D + 0.25D * k;
                zo = random.nextFloat() * 2.0F * k;
            } else {
                x = pos.getX() + 0.5D + 0.25D * k;
                xo = random.nextFloat() * 2.0F * k;
            }

            level.addParticle(ParticleTypes.PORTAL, x, y, z, xo, yo, zo);
        }
    }

    private record PlayerState(long lastInside, long readyAt) {
    }
}
