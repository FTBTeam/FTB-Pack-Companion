package dev.ftb.packcompanion.features.structureplacer;

import dev.ftb.packcompanion.features.structureplacer.network.RequestStructurePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PlacerItem extends Item {
    private static final Map<ResourceLocation, @Nullable ProcessedStructureTemplate> clientStructureCache = new HashMap<>();
    private static final Set<ResourceLocation> requestedStructures = new HashSet<>();
    private static final Map<ResourceLocation, Instant> requestTimestamps = new HashMap<>();

    public PlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var itemStack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(itemStack);
        }

        getStructureServer(itemStack, level).ifPresent(structure -> {
            var lookingAtPos = playerLookingAtPos(player);
            if (lookingAtPos == null) {
                return;
            }

            Rotation rotation = rotationFromPlayerAxis(player);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation);

            var boundingBox = structure.getBoundingBox(settings, lookingAtPos);
            var shiftedPos = axisBasedBlockOffset(player, lookingAtPos, boundingBox);

            structure.placeInWorld((ServerLevelAccessor) level, shiftedPos, shiftedPos, settings, level.getRandom(), 2);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }
        });

        return super.use(level, player, usedHand);
    }

    public static Rotation rotationFromPlayerAxis(Player player) {
        float yaw = player.getYRot() % 360;
        if (yaw < 0) {
            yaw += 360;
        }

        if (yaw >= 315 || yaw < 45) {
            return Rotation.NONE;
        } else if (yaw >= 45 && yaw < 135) {
            return Rotation.CLOCKWISE_90;
        } else if (yaw >= 135 && yaw < 225) {
            return Rotation.CLOCKWISE_180;
        } else {
            return Rotation.COUNTERCLOCKWISE_90;
        }
    }

    public static BlockPos axisBasedBlockOffset(Player player, BlockPos pos, BoundingBox boundingBox) {
        var playerAxis = player.getDirection();

        double shiftedX = 0.0;
        double shiftedZ = 0.0;
        if (playerAxis == Direction.EAST || playerAxis == Direction.WEST) {
            shiftedZ = (boundingBox.getZSpan() + 1) * (playerAxis == Direction.WEST ? -1 : 1);
        } else if (playerAxis == Direction.NORTH || playerAxis == Direction.SOUTH) {
            shiftedX = (boundingBox.getXSpan() + 1) * (playerAxis == Direction.SOUTH ? -1 : 1);
        }

        return pos.offset((int) shiftedX / 2, 0, (int) shiftedZ / 2);
    }

    @Nullable
    public static BlockPos playerLookingAtPos(Player player) {
        var distance = 30;
        var pick = player.pick(distance, 0F, true);
        if (pick instanceof BlockHitResult blockHitResult) {
            if (!player.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
                if (blockHitResult.getDirection() == Direction.UP) {
                    return blockHitResult.getBlockPos().above();
                }

                return blockHitResult.getBlockPos();
            }
        }

        return null;
    }

    public Optional<StructureTemplate> getStructureServer(ItemStack itemStack, Level level) {
        if (!(level instanceof ServerLevel)) {
            throw new IllegalStateException("getStructureServer can only be called on the server side");
        }

        var structureId = getStructureIdFromItem(itemStack);
        if (structureId == null) {
            return Optional.empty();
        }

        var structure = ((ServerLevel) level).getStructureManager().get(structureId).orElse(null);
        return Optional.ofNullable(structure);
    }

    public Optional<ProcessedStructureTemplate> getStructureClient(ItemStack itemStack, Level level) {
        var structureId = getStructureIdFromItem(itemStack);
        if (structureId == null) {
            return Optional.empty();
        }

        // Bypass ones that are loading.
        if (requestedStructures.contains(structureId)) {
            // Has the request timed out? (Give it a second)
            var requestTime = requestTimestamps.get(structureId);
            if (requestTime != null && Instant.now().isAfter(requestTime.plusSeconds(1))) {
                requestedStructures.remove(structureId);
                requestTimestamps.remove(structureId);
            }
            return Optional.empty();
        }

        if (clientStructureCache.containsKey(structureId)) {
            return Optional.ofNullable(clientStructureCache.get(structureId));
        }

        // Ask to load structure
        if (requestedStructures.add(structureId)) {
            requestTimestamps.put(structureId, Instant.now());
        }

        PacketDistributor.sendToServer(new RequestStructurePacket(structureId));
        return Optional.empty();
    }

    public void setStructure(ProcessedStructureTemplate parsedStructure) {
        requestedStructures.remove(parsedStructure.getId());
        clientStructureCache.put(parsedStructure.getId(), parsedStructure);
    }

    public void failedToLoad(ResourceLocation resourceLocation) {
        clientStructureCache.put(resourceLocation, null);
        requestedStructures.remove(resourceLocation);
    }

    @Nullable
    public static ResourceLocation getStructureIdFromItem(ItemStack stack) {
        return stack.get(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT_TYPE.get());
    }

    public static void setStructureId(ResourceLocation structureId, ItemStack stack) {
        stack.set(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT_TYPE.get(), structureId);
    }
}
