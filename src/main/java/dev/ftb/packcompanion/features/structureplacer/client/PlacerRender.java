package dev.ftb.packcompanion.features.structureplacer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import dev.ftb.packcompanion.core.utils.MemorisedValue;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.PosWithRotation;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import dev.ftb.packcompanion.mixin.features.accessor.StructureTemplateMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.*;

public class PlacerRender {
    private static final MemorisedValue<PosWithRotation, Either<Boolean, List<BlockPos>>> canBuild = new MemorisedValue<>();
    private static final PlacerRenderState renderState = new PlacerRenderState();

    private static final Map<Rotation, BlockPos> ROTATIONAL_OFFSET_FIXER = Map.of(
            Rotation.NONE, BlockPos.ZERO,
            Rotation.CLOCKWISE_90,  new BlockPos(-1, 0, 0),
            Rotation.CLOCKWISE_180, new BlockPos(-1, 0, -1),
            Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, 0, -1)
    );

    public static void submitStructurePreview(SubmitCustomGeometryEvent event) {
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

        var placementPos = PlacerItem.placementPos(placerStack, player);
        if (placementPos == null) {
            return;
        }

        var processedTemplate = structure.get();

        Rotation rotation = PlacerItem.rotation(placerStack).orElse(Rotation.NONE);
        var placementBeforeRotation = placementPos.immutable();
        var subtractionOffset = ROTATIONAL_OFFSET_FIXER.get(rotation);
        placementPos = placementPos.subtract(subtractionOffset);

        var template = processedTemplate.getHeldTemplate();

        // Determine if we can build here
        var canBuildOrInvalidLocations = canBuild.get(new PosWithRotation(placementBeforeRotation, rotation), (key) -> PlacerItem.isValidPlacementArea(level, processedTemplate, key));

        var canBuildSimple = canBuildOrInvalidLocations.left().orElse(false);

        int color = ARGB.colorFromFloat(1F, 0F, 1F, 0F); // Green for can build
        if (!canBuildSimple) {
            color = ARGB.colorFromFloat(1F, 1F, 0F, 0F); // Red for cannot build
        }

        var pose = renderState.next(processedTemplate.getId(), placementPos, angleForRotation(rotation));

        var invalidLocations = canBuildOrInvalidLocations.right().orElse(Collections.emptyList());

        PoseStack poseStack = event.getPoseStack();
        var camera = event.getLevelRenderState().cameraRenderState.pos;

        var source = Minecraft.getInstance().renderBuffers().bufferSource();
        var render = source.getBuffer(RenderTypes.LINES);

        poseStack.pushPose();
        poseStack.translate(-camera.x(), -camera.y(), -camera.z());
        poseStack.translate(pose.position().x, pose.position().y, pose.position().z);
        poseStack.mulPose(Axis.YP.rotationDegrees(pose.angleDegrees()));

        List<StructureTemplate.Palette> palettes = ((StructureTemplateMixin) template).getPalettes();
        for (StructureTemplate.Palette palette : palettes) {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                if (info.state().isAir()) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());

                var blockRenderState = new BlockModelRenderState();
                Minecraft.getInstance().getBlockModelResolver().update(blockRenderState, info.state(), BlockDisplayContext.create());

                var worldPos = StructureTemplate.transform(info.pos(), Mirror.NONE, rotation, BlockPos.ZERO).offset(placementBeforeRotation);
                blockRenderState.submit(
                        poseStack,
                        event.getSubmitNodeCollector(),
                        LevelRenderer.getLightCoords(level, worldPos),
                        OverlayTexture.NO_OVERLAY,
                        0
                );

                poseStack.popPose();
            }
        }

        var size = template.getSize();
        ShapeRenderer.renderShape(
                poseStack,
                render,
                Shapes.create(new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ()).inflate(0.01)),
                0, 0, 0,
                color,
                2f
        );

        for (var invalidPos : invalidLocations) {
            ShapeRenderer.renderShape(
                    poseStack,
                    render,
                    Shapes.create(new AABB(0, 0, 0, 1, 1, 1).inflate(0.01)),
                    invalidPos.getX(), invalidPos.getY(), invalidPos.getZ(),
                    color,
                    2f
            );
        }

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
