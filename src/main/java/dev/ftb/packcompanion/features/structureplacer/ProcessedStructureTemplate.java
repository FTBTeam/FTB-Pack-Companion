package dev.ftb.packcompanion.features.structureplacer;

import dev.ftb.packcompanion.mixin.features.accessor.StructureTemplateMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessedStructureTemplate {
    private final StructureTemplate heldTemplate;
    private final Set<BlockPos> solidBlockPositions = new HashSet<>();

    public ProcessedStructureTemplate(StructureTemplate heldTemplate) {
        this.heldTemplate = heldTemplate;
        this.process();
    }

    private void process() {
        List<StructureTemplate.Palette> palettes = ((StructureTemplateMixin) this.heldTemplate).getPalettes();

        // Processing logic goes here
        for (StructureTemplate.Palette palette : palettes) {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                if (info.state().isAir()) {
                    continue;
                }

                solidBlockPositions.add(info.pos());
            }
        }
    }

    public Set<BlockPos> getSolidBlockPositions() {
        return solidBlockPositions;
    }

    public StructureTemplate getHeldTemplate() {
        return heldTemplate;
    }
}
