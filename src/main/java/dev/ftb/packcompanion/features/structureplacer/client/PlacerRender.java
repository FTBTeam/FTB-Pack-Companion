package dev.ftb.packcompanion.features.structureplacer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import dev.ftb.packcompanion.core.utils.MemorisedValue;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.PosWithRotation;
import dev.ftb.packcompanion.features.structureplacer.PotentialPosition;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import dev.ftb.packcompanion.mixin.features.accessor.StructureTemplateMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlacerRender {
    private static final MemorisedValue<PosWithRotation, Either<Boolean, List<BlockPos>>> canBuild = new MemorisedValue<>();
    private static final PlacerRenderState renderState = new PlacerRenderState();

    public static void renderPlacerPreview(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }

        var playerPlacerItem = PlacerItem.getPlacerItemStack(player);
        if (playerPlacerItem.isEmpty()) {
            return;
        }

        var placerItem = (PlacerItem) playerPlacerItem.get().getItem();
        var placerStack = playerPlacerItem.get();

        Optional<ProcessedStructureTemplate> structure = placerItem.getStructureClient(placerStack, level);
        if (structure.isEmpty()) {
            return;
        }

        renderPreview(event, placerStack, placerItem, structure.get(), player, level);
    }

    private static void renderPreview(RenderLevelStageEvent event, ItemStack stack, PlacerItem placerItem, ProcessedStructureTemplate processedTemplate, Player player, Level level) {
        var placementPos = PlacerItem.placementPos(stack, player);
        if (placementPos == null) {
            return;
        }

        var nudgeOffset = PlacerItem.nudgeOffset(stack).orElse(BlockPos.ZERO);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        var source = Minecraft.getInstance().renderBuffers().bufferSource();
        var render = source.getBuffer(RenderType.LINES);

        Rotation rotation = PlacerItem.rotation(stack).orElse(Rotation.NONE);

        var template = processedTemplate.getHeldTemplate();

        // Determine if we can build here
        var canBuildOrInvalidLocations = canBuild.get(new PosWithRotation(placementPos, rotation), (key) -> PlacerItem.isValidPlacementArea(level, processedTemplate, key));

        var canBuildSimple = canBuildOrInvalidLocations.left().orElse(false);

        // Ease the rendered ghost toward the snapped target position/rotation instead of jumping instantly.
        // The snapped values above still drive canBuildHere and actual placement.
        var pose = renderState.next(processedTemplate.getId(), placementPos, angleForRotation(rotation), nudgeOffset);

        var easedNudge = pose.nudgeOffset();
        poseStack.pushPose();
        poseStack.translate(pose.position().x - easedNudge.x(), pose.position().y - easedNudge.y(), pose.position().z - easedNudge.z());
        LevelRenderer.renderVoxelShape(
                poseStack,
                render,
                Shapes.block(),
                0, 0, 0,
                0f, 0f, 1f,
                1f, false
        );
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(pose.position().x, pose.position().y, pose.position().z);
        poseStack.mulPose(Axis.YP.rotationDegrees(pose.angleDegrees()));
        poseStack.scale(1.001f, 1.001f, 1.001f); // Slightly scale up the outline so it doesn't Z-fight with the blocks below

        // Render the outline in the structure's own (unrotated) local space too, so it rides the same
        // tweened transform as the blocks below instead of snapping ahead of them mid-rotation.
        var size = template.getSize();
        LevelRenderer.renderVoxelShape(
                poseStack,
                render,
                Shapes.create(new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ())),
                0, 0, 0,
                canBuildSimple ? 0.0f : 1f, canBuildSimple ? 1.0f : 0f, 0.0f,
                1f, false
        );

        var invalidLocations = canBuildOrInvalidLocations.right().orElse(Collections.emptyList());
        for (var invalidPos : invalidLocations) {
            LevelRenderer.renderVoxelShape(
                    poseStack,
                    render,
                    Shapes.block(),
                    invalidPos.getX(), invalidPos.getY(), invalidPos.getZ(),
                    1f, 0f, 0f,
                    1f, false
            );
        }

        List<StructureTemplate.Palette> palettes = ((StructureTemplateMixin) template).getPalettes();
        for (StructureTemplate.Palette palette : palettes) {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                if (info.state().isAir()) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        info.state(),
                        poseStack,
                        source,
                        15728880,
                        OverlayTexture.NO_OVERLAY
                );

                poseStack.popPose();
            }
        }

        poseStack.popPose();
        poseStack.popPose();

        source.endBatch();
    }

    private static float angleForRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0F;
            case CLOCKWISE_90 -> -90F;
            case CLOCKWISE_180 -> 180F;
            case COUNTERCLOCKWISE_90 -> 90F;
        };
    }
}
