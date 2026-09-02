package dev.ftb.packcompanion.features.structureplacer;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.structureplacer.client.StructurePlacerFeatureClient;
import dev.ftb.packcompanion.features.structureplacer.network.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Adds an item that is able to place a configured structure in the world.
 */
public class StructurePlacerFeature extends Feature.Common {
    private static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTRY = getRegistry(Registries.DATA_COMPONENT_TYPE);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlacerDataComponent>> STRUCTURE_PLACER_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTRY.register("placer_data", (b) ->
            DataComponentType.<PlacerDataComponent>builder()
                    .persistent(PlacerDataComponent.CODEC)
                    .networkSynchronized(PlacerDataComponent.STREAM_CODEC)
                    .build()
    );

    public static final DeferredHolder<Item, PlacerItem> STRUCTURE_PLACER = ITEM_REGISTRY.register("structure_placer", () ->
            new PlacerItem(new Item.Properties().stacksTo(1))
    );

    public StructurePlacerFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (FMLEnvironment.dist.isClient()) {
            StructurePlacerFeatureClient.init(modEventBus);
        }
    }

    @Override
    public void registerPackets(PayloadRegistrar registrar) {
        registrar.playToServer(RequestStructurePacket.TYPE, RequestStructurePacket.STREAM_CODEC, RequestStructurePacket::handle);
        registrar.playToClient(ProvideStructurePacket.TYPE, ProvideStructurePacket.STREAM_CODEC, ProvideStructurePacket::handle);

        registrar.playToServer(AnchorPlacerPacket.TYPE, AnchorPlacerPacket.STREAM_CODEC, AnchorPlacerPacket::handle);
        registrar.playToServer(RotatePlacerPacket.TYPE, RotatePlacerPacket.STREAM_CODEC, RotatePlacerPacket::handle);
        registrar.playToServer(NudgePacket.TYPE, NudgePacket.STREAM_CODEC, NudgePacket::handle);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();

        translations.addItem(STRUCTURE_PLACER, "Structure Placer");

        collector.addItemModelProvider(provider -> {
            provider.generateFlatItem(STRUCTURE_PLACER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        });
    }
}
