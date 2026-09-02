package dev.ftb.packcompanion.features.structureplacer;

import com.mojang.datafixers.util.Either;
import dev.ftb.packcompanion.features.structureplacer.client.PlacerRender;
import dev.ftb.packcompanion.features.structureplacer.network.RequestStructurePacket;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class PlacerItem extends Item {
    private static final Map<ResourceLocation, @Nullable ProcessedStructureTemplate> clientStructureCache = new HashMap<>();
    private static final Set<ResourceLocation> requestedStructures = new HashSet<>();
    private static final Map<ResourceLocation, Instant> requestTimestamps = new HashMap<>();

    public PlacerItem(Properties properties) {
        super(properties.component(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        var oldData = oldStack.get(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get());
        var newData = newStack.get(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get());

        return !Objects.equals(oldData, newData);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var itemStack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(itemStack);
        }

        getStructureServer(itemStack, level).ifPresent(structurePair -> {
            var structureId = structurePair.left();
            var structure = structurePair.right();

            var placementPos = placementPos(itemStack, player);
            if (placementPos == null) {
                return;
            }

            Rotation rotation = rotation(itemStack).orElse(Rotation.NONE);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation);

            // Verify if the placement area is valid
            var posWithRotation = new PosWithRotation(placementPos, rotation);
            var placementCheck = PlacerItem.isValidPlacementArea(level, new ProcessedStructureTemplate(structureId, structure), posWithRotation);
            if (!placementCheck.left().orElse(false)) {
                // TODO: translation
                player.displayClientMessage(Component.literal("Invalid placement area!"), true);
                return;
            }

            structure.placeInWorld((ServerLevelAccessor) level, placementPos, placementPos, settings, level.getRandom(), Block.UPDATE_ALL);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }
        });

        return super.use(level, player, usedHand);
    }

    @Nullable
    public static BlockPos placementPos(ItemStack stack, Player player) {
        var nudge = nudgeOffset(stack).orElse(BlockPos.ZERO);

        var anchor = anchorPos(stack);
        if (anchor.isPresent()) {
            return anchor.get().offset(nudge);
        }

        var blockPos = blockPosFromPick(player);
        if (blockPos != null) {
            return blockPos.offset(nudge);
        }

        return null;
    }

    public static BlockPos blockPosFromPick(Player player) {
        var pick = player.pick(30, 0F, true);
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

    public Optional<Pair<ResourceLocation, StructureTemplate>> getStructureServer(ItemStack itemStack, Level level) {
        if (!(level instanceof ServerLevel)) {
            throw new IllegalStateException("getStructureServer can only be called on the server side");
        }

        var structureId = getStructureIdFromItem(itemStack);
        if (structureId == null) {
            return Optional.empty();
        }

        var structure = ((ServerLevel) level).getStructureManager().get(structureId).orElse(null);
        return Optional.of(Pair.of(structureId, structure));
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
        PlacerDataComponent placerDataComponent = stack.get(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get());
        if (placerDataComponent == null) {
            return null;
        }

        return placerDataComponent.structureId();
    }

    public static void setStructureId(ResourceLocation structureId, ItemStack stack) {
        var data = stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY);
        stack.set(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), data.withStructureId(structureId));
    }

    public static Optional<BlockPos> anchorPos(ItemStack stack) {
        return stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY)
                .anchorPos();
    }

    public static void setAnchorPos(ItemStack stack, @Nullable BlockPos anchorPos) {
        PlacerDataComponent placerDataComponent = stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY);
        stack.set(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), placerDataComponent.withAnchorPos(anchorPos));
    }

    public static Optional<Rotation> rotation(ItemStack stack) {
        return stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY)
                .rotation();
    }

    public static void setRotation(ItemStack stack, @Nullable Rotation rotation) {
        PlacerDataComponent placerDataComponent = stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY);
        stack.set(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), placerDataComponent.withRotation(rotation));
    }

    public static Optional<BlockPos> nudgeOffset(ItemStack stack) {
        return stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY)
                .nudgeOffset();
    }

    public static void setNudgeOffset(ItemStack stack, @Nullable BlockPos nudgeOffset) {
        PlacerDataComponent placerDataComponent = stack.getOrDefault(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), PlacerDataComponent.EMPTY);
        stack.set(StructurePlacerFeature.STRUCTURE_PLACER_DATA_COMPONENT.get(), placerDataComponent.withNudgeOffset(nudgeOffset));
    }

    public static Optional<ItemStack> getPlacerItemStack(Player player) {
        var itemInHand = player.getMainHandItem();
        if (itemInHand.is(StructurePlacerFeature.STRUCTURE_PLACER.get())) {
            return Optional.of(itemInHand);
        }

        var itemInOffHand = player.getOffhandItem();
        if (itemInOffHand.is(StructurePlacerFeature.STRUCTURE_PLACER.get())) {
            return Optional.of(itemInOffHand);
        }

        return Optional.empty();
    }

    public static Either<Boolean, List<BlockPos>> isValidPlacementArea(Level level, ProcessedStructureTemplate template, PosWithRotation pos) {
        var solidBlocks = template.getSolidBlockPositions().stream()
                .map(inputPos -> {
                    var transformedPos = StructureTemplate.transform(inputPos, Mirror.NONE, pos.rotation(), BlockPos.ZERO)
                            .offset(pos.pos());
                    return new PotentialPosition(inputPos, transformedPos, level.getBlockState(transformedPos));
                }).toList();

        List<BlockPos> invalidLocations = new ArrayList<>();
        for (var potentialPos : solidBlocks) {
            var state = potentialPos.state();
            if (!state.isAir() && !state.canBeReplaced()) {
                invalidLocations.add(potentialPos.inStructure());
            }
        }

        if (!invalidLocations.isEmpty()) {
            return Either.right(invalidLocations);
        }

        return Either.left(true);
    }
}
