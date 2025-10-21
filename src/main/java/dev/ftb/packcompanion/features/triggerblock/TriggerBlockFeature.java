package dev.ftb.packcompanion.features.triggerblock;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TriggerBlockFeature extends Feature.Common {
    public static final DeferredRegister<Block> BLOCK_REGISTRY = getRegistry(Registries.BLOCK);
    public static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTRY = getRegistry(Registries.BLOCK_ENTITY_TYPE);

    public static final DeferredHolder<Block, TriggerBlock> TRIGGER_BLOCK = BLOCK_REGISTRY.register("trigger_block", () -> new TriggerBlock(BlockBehaviour.Properties.of()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TriggerBlockEntity>> TRIGGER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPE_REGISTRY.register("trigger_block", () -> BlockEntityType.Builder.of(TriggerBlockEntity::new, TRIGGER_BLOCK.get()).build(null));

    static {
        ITEM_REGISTRY.register("trigger_block", () -> new BlockItem(TRIGGER_BLOCK.get(), new Item.Properties()));
    }

    public TriggerBlockFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::onServerLevelTick);
    }

    private void onServerLevelTick(ServerTickEvent.Post event) {
        TriggerBlockController.INSTANCE.onTick();
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();
        translations.addBlock(TRIGGER_BLOCK, "Player Trigger");

        collector.addBlockStateProvider(provider -> {
            provider.simpleBlock(TRIGGER_BLOCK.get());
        });

        collector.addItemModelProvider(provider -> {
            provider.simpleBlockItem(TRIGGER_BLOCK.get());
        });
    }
}
