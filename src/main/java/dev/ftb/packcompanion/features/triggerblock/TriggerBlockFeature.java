package dev.ftb.packcompanion.features.triggerblock;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TriggerBlockFeature extends Feature.Common {
    public static final DeferredRegister<Block> BLOCK_REGISTRY = getRegistry(Registries.BLOCK);
    public static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTRY = getRegistry(Registries.BLOCK_ENTITY_TYPE);

    public static final RegistryObject<TriggerBlock> TRIGGER_BLOCK = BLOCK_REGISTRY.register("trigger_block", () -> new TriggerBlock(BlockBehaviour.Properties.of()));
    public static final RegistryObject<BlockEntityType<TriggerBlockEntity>> TRIGGER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPE_REGISTRY.register("trigger_block", () -> BlockEntityType.Builder.of(TriggerBlockEntity::new, TRIGGER_BLOCK.get()).build(null));

    static {
        ITEM_REGISTRY.register("trigger_block", () -> new BlockItem(TRIGGER_BLOCK.get(), new Item.Properties()));
    }

    public TriggerBlockFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TriggerBlockController.getInstance(false).onTick();
        }
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TriggerBlockController.getInstance(true).onTick();
        }
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();
        translations.addBlock(TRIGGER_BLOCK, "Player Trigger");

        collector.addBlockStateProvider(provider -> {
            provider.simpleBlockWithItem(TRIGGER_BLOCK.get(), provider.cubeAll(TRIGGER_BLOCK.get()));
        });
    }
}
