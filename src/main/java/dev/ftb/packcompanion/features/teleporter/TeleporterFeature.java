package dev.ftb.packcompanion.features.teleporter;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.teleporter.client.TeleporterClient;
import dev.ftb.packcompanion.features.teleporter.net.OpenTeleporterPacket;
import dev.ftb.packcompanion.features.teleporter.net.RunTeleporterAction;
import dev.ftb.packcompanion.features.teleporter.net.TryOpenTeleporterFromItemPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.CuriosTags;

public class TeleporterFeature extends Feature.Common {
    private static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredHolder<Item, TeleporterItem> TELEPORTER_ITEM = ITEM_REGISTRY.register("teleporter", () ->
            new TeleporterItem(new Item.Properties().stacksTo(1))
    );

    public TeleporterFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::onClientInit);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(TeleporterClient::onRegisterKeyBindings);
        }
    }

    private void onClientInit(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(TeleporterClient::onInputEvent);
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        // Reload destinations
        TeleporterDestinations.get().load();
    }

    @Override
    public void registerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(OpenTeleporterPacket.TYPE, OpenTeleporterPacket.STREAM_CODEC, OpenTeleporterPacket::handle);
        registrar.playToServer(RunTeleporterAction.TYPE, RunTeleporterAction.STREAM_CODEC, RunTeleporterAction::handle);
        registrar.playToServer(TryOpenTeleporterFromItemPacket.TYPE, TryOpenTeleporterFromItemPacket.STREAM_CODEC, TryOpenTeleporterFromItemPacket::handle);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();

        translations.addItem(TELEPORTER_ITEM, "Teleporter");
        translations.prefixed("home", "Home");
        translations.prefixed("spawn", "Spawn");

        translations.prefixed("key.category", "Pack Companion");
        translations.prefixed("key.open_teleporter", "Open Teleporter");

        collector.addItemModelProvider(provider -> {
            provider.basicItem(TELEPORTER_ITEM.get());
        });

        collector.addItemTagProvider(provider -> {
            provider.appendItemTag(CuriosTags.CURIO).add(TELEPORTER_ITEM.get());
        });
    }
}
