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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class PlacerItem extends Item {
    private final LoadedValue<ProcessedStructureTemplate> structure = new LoadedValue<>();
    private boolean requestedStructure = false;

    private ResourceLocation structureLocation = ResourceLocation.withDefaultNamespace("pillager_outpost/watchtower");

    public PlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }

        getStructure(level).ifPresent(processedStructure -> {
            var lookingAtPos = playerLookingAtPos(player);
            if (lookingAtPos == null) {
                return;
            }

            var structure = processedStructure.getHeldTemplate();

            Rotation rotation = rotationFromPlayerAxis(player);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation);

            var boundingBox = structure.getBoundingBox(settings, lookingAtPos);
            var shiftedPos = axisBasedBlockOffset(player, lookingAtPos, boundingBox);

            structure.placeInWorld((ServerLevelAccessor) level, shiftedPos, shiftedPos, settings, level.getRandom(), 2);
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

    public Optional<ProcessedStructureTemplate> getStructure(Level level) {
        if (requestedStructure) {
            return Optional.empty();
        }

        if (!structure.isLoaded()) {
            if (level instanceof ServerLevel) {
                System.out.println("Loading structure directly from server: " + this.structureLocation);
                structure.loadValue(() -> {
                    var structure = ((ServerLevel) level).getStructureManager().get(this.structureLocation).orElse(null);
                    if (structure == null) {
                        return null;
                    }

                    return new ProcessedStructureTemplate(structure);
                });
            } else {
                System.out.println("Requesting structure from server: " + this.structureLocation);
                requestedStructure = true;
                // Request structure from server
                PacketDistributor.sendToServer(new RequestStructurePacket(this.structureLocation));
            }

            return Optional.empty();
        }

        return Optional.ofNullable(structure.getValue());
    }

    public void setStructure(ProcessedStructureTemplate parsedStructure) {
        this.structure.updateValue(parsedStructure);
        if (this.requestedStructure) {
            requestedStructure = false;
        }
    }

    private static class LoadedValue<T> {
        public @Nullable T value = null;
        public boolean loaded = false;

        public boolean isLoaded() {
            return loaded;
        }

        public void loadValue(Supplier<T> supplier) {
            if (!loaded) {
                value = supplier.get();
                loaded = true;
            }

            throw new IllegalStateException("Value already loaded");
        }

        public void updateValue(T newValue) {
            this.value = newValue;
            this.loaded = true;
        }

        public @Nullable T getValue() {
            return value;
        }
    }
}
