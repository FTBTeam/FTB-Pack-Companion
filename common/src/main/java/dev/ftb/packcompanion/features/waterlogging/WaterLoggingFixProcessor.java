package dev.ftb.packcompanion.features.waterlogging;

import com.mojang.serialization.Codec;
import dev.ftb.packcompanion.registry.StructureProcessorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class WaterLoggingFixProcessor extends StructureProcessor {
    private static final WaterLoggingFixProcessor INSTANCE = new WaterLoggingFixProcessor();
    public static final Codec<WaterLoggingFixProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo after, StructurePlaceSettings structurePlaceSettings) {
        if (after.state.isAir() || !(levelReader instanceof WorldGenRegion worldGenRegion)) {
            return after;
        }

        // Is the block meant to be a fluid? No then lets make sure the world doesn't already have a fluid there
        if (after.state.getFluidState().isEmpty()) {
            // Is the block water? No, is the current block in the world water? Yes, Cool, remove it before placement
            if (after.state.getBlock() != Blocks.WATER && levelReader.getBlockState(after.pos).getFluidState().is(Fluids.WATER)) {
                worldGenRegion.setBlock(after.pos, after.state, Block.UPDATE_ALL);
            }
        }

        return after;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorRegistry.WATER_LOGGING_FIX_PROCESSOR.get();
    }
}
