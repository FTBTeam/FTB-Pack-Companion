package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.features.grid.GridStructurePlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public interface StructurePlacementRegistry {
    DeferredRegister<StructurePlacementType<?>> REGISTRY = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registries.STRUCTURE_PLACEMENT);

    RegistrySupplier<StructurePlacementType<GridStructurePlacement>> GRID_PLACEMENT = REGISTRY.register("grid_placement", () -> () -> GridStructurePlacement.CODEC);
}