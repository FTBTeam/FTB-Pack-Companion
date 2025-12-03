package dev.ftb.packcompanion.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.features.triggerblock.TriggerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlocksRegistries {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(PackCompanionAPI.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<TriggerBlock> TRIGGER_BLOCK = BLOCKS.register("trigger_block", () -> new TriggerBlock(Block.Properties.of()));
}
