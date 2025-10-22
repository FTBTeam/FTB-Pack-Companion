package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import dev.ftb.packcompanion.features.actionpad.net.OpenActionPadPacket;
import dev.ftb.packcompanion.features.actionpad.net.RunActionPacket;
import dev.ftb.packcompanion.features.actionpad.net.TryOpenActionPadFromItemPacket;
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

public class ActionPadFeature extends Feature.Common {
    private static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredHolder<Item, ActionPadItem> ACTION_PAD = ITEM_REGISTRY.register("action_pad", () ->
            new ActionPadItem(new Item.Properties().stacksTo(1))
    );

    public ActionPadFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::onClientInit);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ActionPadClient::onRegisterKeyBindings);
        }
    }

    private void onClientInit(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ActionPadClient::onInputEvent);
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        // Reload actions
        PadActions.get().load();
    }

    @Override
    public void registerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(OpenActionPadPacket.TYPE, OpenActionPadPacket.STREAM_CODEC, OpenActionPadPacket::handle);
        registrar.playToServer(RunActionPacket.TYPE, RunActionPacket.STREAM_CODEC, RunActionPacket::handle);
        registrar.playToServer(TryOpenActionPadFromItemPacket.TYPE, TryOpenActionPadFromItemPacket.STREAM_CODEC, TryOpenActionPadFromItemPacket::handle);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();

        translations.addItem(ACTION_PAD, "Action Pad");
        translations.prefixed("home", "Home");
        translations.prefixed("spawn", "Spawn");

        translations.prefixed("key.category", "Pack Companion");
        translations.prefixed("key.open_action_pad", "Open Action Pad");

        collector.addItemModelProvider(provider -> {
            provider.basicItem(ACTION_PAD.get());
        });

        collector.addItemTagProvider(provider -> {
            provider.appendItemTag(CuriosTags.CURIO).add(ACTION_PAD.get());
        });
    }
}
