package dev.ftb.packcompanion.features.structureplacer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record PotentialPosition(BlockPos inStructure, BlockPos inWorld, BlockState state) {}
