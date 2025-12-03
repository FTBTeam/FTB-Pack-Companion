package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.features.triggerblock.TriggerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<TriggerBlockEntity>> TRIGGER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY.register("trigger_block", () -> BlockEntityType.Builder.of(TriggerBlockEntity::new, BlocksRegistries.TRIGGER_BLOCK.get()).build(null));

}
