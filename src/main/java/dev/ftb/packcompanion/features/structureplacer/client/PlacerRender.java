package dev.ftb.packcompanion.features.structureplacer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.packcompanion.core.utils.MemorisedValue;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import dev.ftb.packcompanion.mixin.features.accessor.StructureTemplateMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;
import java.util.Optional;

public class PlacerRender {
    private static final MemorisedValue<BlockPos, Boolean> canBuild = new MemorisedValue<>();

    public static void renderPlacerPreview(RenderLevelStageEvent.AfterTranslucentParticles event) {
        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }

        var itemInHand = player.getMainHandItem();
        var itemInOffHand = player.getOffhandItem();

        PlacerItem placerItem = null;
        ItemStack activeItem = ItemStack.EMPTY;
        if (itemInHand.getItem() instanceof PlacerItem) {
            placerItem = (PlacerItem) itemInHand.getItem();
            activeItem = itemInHand;
        } else if (itemInOffHand.getItem() instanceof PlacerItem) {
            placerItem = (PlacerItem) itemInOffHand.getItem();
            activeItem = itemInOffHand;
        }

        if (placerItem == null) {
            return;
        }

        Optional<ProcessedStructureTemplate> structure = placerItem.getStructureClient(activeItem, level);
        if (structure.isEmpty()) {
            return;
        }

        renderPreview(event, placerItem, structure.get(), player, level);
    }

    private static void renderPreview(RenderLevelStageEvent event, PlacerItem placerItem, ProcessedStructureTemplate processedTemplate, Player player, Level level) {
        var lookingAtPos = PlacerItem.playerLookingAtPos(player);
        if (lookingAtPos == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        var camera = event.getLevelRenderState().cameraRenderState.pos;
        poseStack.translate(-camera.x(), -camera.y(), -camera.z());

        var source = Minecraft.getInstance().renderBuffers().bufferSource();
        var render = source.getBuffer(RenderTypes.LINES);

        Rotation rotation = PlacerItem.rotationFromPlayerAxis(player);

        var template = processedTemplate.getHeldTemplate();

        BoundingBox boundingBox = template.getBoundingBox(BlockPos.ZERO, rotation, BlockPos.ZERO, Mirror.NONE);
        var shiftedLookingAt = PlacerItem.axisBasedBlockOffset(player, lookingAtPos, boundingBox);

        // Determine if we can build here
        var canBuildHere = canBuild.get(shiftedLookingAt, (pos) -> processedTemplate.getSolidBlockPositions().stream()
                .map(inputPos -> {
                    var transformedPos = StructureTemplate.transform(inputPos, Mirror.NONE, rotation, BlockPos.ZERO)
                            .offset(pos);
                    return level.getBlockState(transformedPos);
                })
                .noneMatch(state -> !state.isAir() && !state.canBeReplaced()));

        int color = ARGB.colorFromFloat(1F, 0F, 1F, 0F); // Green for can build
        if (!canBuildHere) {
            color = ARGB.colorFromFloat(1F, 1F, 0F, 0F); // Red for cannot build
        }

        SubmitNodeStorage submitNodeStorage = Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();

        // Render the outline.
        ShapeRenderer.renderShape(
                event.getPoseStack(),
                render,
                Shapes.create(new AABB(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.maxX() + 1, boundingBox.maxY() + 1 , boundingBox.maxZ() + 1)),
                shiftedLookingAt.getX(), shiftedLookingAt.getY(),
                shiftedLookingAt.getZ(),
                color,
                2f
        );

        poseStack.popPose();
        source.endBatch();

        poseStack.pushPose();
        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.translate(-camera.x(), -camera.y(), -camera.z());

        List<StructureTemplate.Palette> palettes = ((StructureTemplateMixin) template).getPalettes();
        for (StructureTemplate.Palette palette : palettes) {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                if (info.state().isAir()) {
                    continue;
                }

                poseStack.pushPose();

                var pos = StructureTemplate.transform(info.pos(), Mirror.NONE, rotation, BlockPos.ZERO).offset(shiftedLookingAt);
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                var renderState = new BlockModelRenderState();
                Minecraft.getInstance().getBlockModelResolver().update(renderState, info.state(), BlockDisplayContext.create());

                renderState.submit(
                        poseStack,
                        submitNodeStorage,
                        LevelRenderer.getLightCoords(level, pos),
                        OverlayTexture.NO_OVERLAY,
                        0
                );

                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
}
