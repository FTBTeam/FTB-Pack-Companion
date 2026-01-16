package dev.ftb.packcompanion.features.structureplacer;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.structureplacer.client.PlacerRender;
import dev.ftb.packcompanion.features.structureplacer.network.ProvideStructurePacket;
import dev.ftb.packcompanion.features.structureplacer.network.RequestStructurePacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Adds an item that is able to place a configured structure in the world.
 */
public class StructurePlacerFeature extends Feature.Common {
    private static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredHolder<Item, PlacerItem> STRUCTURE_PLACER = ITEM_REGISTRY.register("structure_placer", () ->
            new PlacerItem(new Item.Properties().stacksTo(1))
    );

    public StructurePlacerFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(PlacerRender::renderPlacerPreview);
        }
    }

    @Override
    public void registerPackets(PayloadRegistrar registrar) {
        registrar.playToServer(RequestStructurePacket.TYPE, RequestStructurePacket.STREAM_CODEC, RequestStructurePacket::handle);
        registrar.playToClient(ProvideStructurePacket.TYPE, ProvideStructurePacket.STREAM_CODEC, ProvideStructurePacket::handle);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();

        translations.addItem(STRUCTURE_PLACER, "Structure Placer");

        collector.addItemModelProvider(provider -> {
            provider.basicItem(STRUCTURE_PLACER.get());
        });
    }
}
