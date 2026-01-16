package dev.ftb.packcompanion.features.structureplacer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.packcompanion.core.utils.MemorisedValue;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import dev.ftb.packcompanion.mixin.features.accessor.StructureTemplateMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
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

    public static void renderPlacerPreview(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }

        var itemInHand = player.getMainHandItem();
        var itemInOffHand = player.getOffhandItem();

        PlacerItem placerItem = null;
        if (itemInHand.getItem() instanceof PlacerItem) {
            placerItem = (PlacerItem) itemInHand.getItem();
        } else if (itemInOffHand.getItem() instanceof PlacerItem) {
            placerItem = (PlacerItem) itemInOffHand.getItem();
        }

        if (placerItem == null) {
            return;
        }

        Optional<ProcessedStructureTemplate> structure = placerItem.getStructure(level);
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

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        var source = Minecraft.getInstance().renderBuffers().bufferSource();
        var render = source.getBuffer(RenderType.LINES);

        Rotation rotation = PlacerItem.rotationFromPlayerAxis(player);

        var template = processedTemplate.getHeldTemplate();

        BoundingBox boundingBox = template.getBoundingBox(BlockPos.ZERO, rotation, BlockPos.ZERO, Mirror.NONE);
        var shiftedLookingAt = PlacerItem.axisBasedBlockOffset(player, lookingAtPos, boundingBox);

        var canBuildHere = canBuild.get(shiftedLookingAt, (pos) -> {
            var res = processedTemplate.getSolidBlockPositions().stream()
                    .map(inputPos -> {
                        var transformedPos = StructureTemplate.transform(inputPos, Mirror.NONE, rotation, BlockPos.ZERO)
                                .offset(pos);
                        return level.getBlockState(transformedPos);
                    })
                    .noneMatch(state -> {
                        var r = !state.isAir() && !state.canBeReplaced();
                        if (r) {
                            System.out.println("Cannot build at " + pos + " because of block " + state);
                        }
                        return r;
                    });

            return res;
        });

        LevelRenderer.renderVoxelShape(
                event.getPoseStack(),
                render,
                Shapes.create(new AABB(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.maxX(), boundingBox.maxY() + 1 , boundingBox.maxZ())),
                shiftedLookingAt.getX(), shiftedLookingAt.getY(),
                shiftedLookingAt.getZ(),
                canBuildHere ? 0.0f : 1f, canBuildHere ? 1.0f : 0f, 0.0f,
                1f, false
        );

        List<StructureTemplate.Palette> palettes = ((StructureTemplateMixin) template).getPalettes();
        for (StructureTemplate.Palette palette : palettes) {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                if (info.state().isAir()) {
                    continue;
                }

                poseStack.pushPose();

                var pos = StructureTemplate.transform(info.pos(), Mirror.NONE, rotation, BlockPos.ZERO).offset(shiftedLookingAt);
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        info.state(),
                        poseStack,
                        source,
                        LevelRenderer.getLightColor(level, info.pos()),
                        OverlayTexture.NO_OVERLAY
                );

                poseStack.popPose();
            }
        }

        poseStack.popPose();

        source.endBatch();
    }
}
