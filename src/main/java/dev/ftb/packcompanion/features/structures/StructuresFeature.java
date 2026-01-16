package dev.ftb.packcompanion.features.structures;

import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.registries.RegistryObject;

public class StructuresFeature extends Feature.Common {
    public static final RegistryObject<StructureProcessorType<WaterLoggingFixProcessor>> WATER_LOGGING_FIX_PROCESSOR =
            getRegistry(Registries.STRUCTURE_PROCESSOR)
                    .register("waterlogging_fix_processor", () -> () -> WaterLoggingFixProcessor.CODEC);

    public static final RegistryObject<StructurePlacementType<GridStructurePlacement>> GRID_PLACEMENT =
            getRegistry(Registries.STRUCTURE_PLACEMENT)
                    .register("grid_placement", () -> () -> GridStructurePlacement.CODEC);

    public StructuresFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }
}
