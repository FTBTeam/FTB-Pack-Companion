package dev.ftb.packcompanion.features.structures;

import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StructuresFeature extends Feature.Common {
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<WaterLoggingFixProcessor>> WATER_LOGGING_FIX_PROCESSOR =
            getRegistry(Registries.STRUCTURE_PROCESSOR).register("waterlogging_fix_processor", () -> () -> WaterLoggingFixProcessor.CODEC);

    public StructuresFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }
}
