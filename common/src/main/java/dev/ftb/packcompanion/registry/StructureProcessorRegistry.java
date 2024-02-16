package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.features.waterlogging.WaterLoggingFixProcessor;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public interface StructureProcessorRegistry {
    DeferredRegister<StructureProcessorType<?>> REGISTRY = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registry.STRUCTURE_PROCESSOR_REGISTRY);

    RegistrySupplier<StructureProcessorType<WaterLoggingFixProcessor>> WATER_LOGGING_FIX_PROCESSOR = REGISTRY.register("waterlogging_fix_processor", () -> () -> WaterLoggingFixProcessor.CODEC);
}
