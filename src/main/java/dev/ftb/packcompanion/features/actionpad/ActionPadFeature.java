package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.features.actionpad.client.ActionPadClient;
import dev.ftb.packcompanion.features.actionpad.net.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.atomic.AtomicInteger;

public class ActionPadFeature extends Feature.Common {
    private static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final RegistryObject<ActionPadItem> ACTION_PAD = ITEM_REGISTRY.register("action_pad", () ->
            new ActionPadItem(new Item.Properties().stacksTo(1))
    );

    public ActionPadFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::onClientInit);
        modEventBus.addListener(this::creativeTab);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ActionPadClient::onRegisterKeyBindings);
        }
    }

    private void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ACTION_PAD.get());
        }
    }

    private void onClientInit(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ActionPadClient::onInputEvent);
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        // Reload actions
        PadActions.get().load();
    }

    @Override
    public void registerPackets(SimpleChannel channel, AtomicInteger packetId) {
        // TODO: Finish implementation of action pad packets
        channel.registerMessage(packetId.getAndIncrement(), OpenActionPadPacket.class, OpenActionPadPacket::encode, OpenActionPadPacket::decode, OpenActionPadPacket::handle);

        channel.registerMessage(packetId.getAndIncrement(), OpenTPAPacket.class, OpenTPAPacket::encode, OpenTPAPacket::decode, OpenTPAPacket::handle);
        channel.registerMessage(packetId.getAndIncrement(), RunActionPacket.class, RunActionPacket::encode, RunActionPacket::decode, RunActionPacket::handle);
        channel.registerMessage(packetId.getAndIncrement(), TryOpenActionPadFromItemPacket.class, TryOpenActionPadFromItemPacket::encode, TryOpenActionPadFromItemPacket::decode, TryOpenActionPadFromItemPacket::handle);
        channel.registerMessage(packetId.getAndIncrement(), TryOpenActionTPAPacket.class, TryOpenActionTPAPacket::encode, TryOpenActionTPAPacket::decode, TryOpenActionTPAPacket::handle);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();

        translations.addItem(ACTION_PAD, "Action Pad");
        translations.prefixed("home", "Home");
        translations.prefixed("spawn", "Spawn");
        translations.prefixed("tpa", "Request Teleport");
        translations.prefixed("actionpad.tpa.request_sent", "TP request sent to");

        translations.prefixed("key.category", "Pack Companion");
        translations.prefixed("key.open_action_pad", "Open Action Pad");

        collector.addItemModelProvider(provider -> {
            provider.basicItem(ACTION_PAD.get());
        });

        collector.addItemTagProvider(provider -> {
            var tagKey = TagKey.create(Registries.ITEM, new ResourceLocation("curios:curio"));
            provider.appendItemTag(tagKey).add(ACTION_PAD.get());
        });
    }
}
