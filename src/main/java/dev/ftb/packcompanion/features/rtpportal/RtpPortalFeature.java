package dev.ftb.packcompanion.features.rtpportal;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.io.IOException;

import static com.mojang.logging.LogUtils.getLogger;

public class RtpPortalFeature extends Feature.Common {
    public static final String ESSENTIALS_MOD_ID = "ftbessentials";
    public static final boolean ENABLED = ModList.get().isLoaded(ESSENTIALS_MOD_ID);

    private static final Logger LOGGER = getLogger();

    public static final DeferredRegister<Block> BLOCK_REGISTRY = getRegistry(Registries.BLOCK);
    public static final DeferredRegister<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    public static final DeferredRegister<SoundEvent> SOUND_REGISTRY = getRegistry(Registries.SOUND_EVENT);

    public static final DeferredHolder<Block, RtpPortalBlock> RTP_PORTAL_BLOCK = BLOCK_REGISTRY.register("rtp_portal", (id) -> new RtpPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).explosionResistance(3600000.0F).setId(ResourceKey.create(Registries.BLOCK, id))));
    public static final DeferredHolder<Item, BlockItem> RTP_PORTAL_ITEM = ITEM_REGISTRY.register("rtp_portal", (id) -> new BlockItem(RTP_PORTAL_BLOCK.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredHolder<SoundEvent, SoundEvent> RTP_PORTAL_SOUND = SOUND_REGISTRY.register("rtp_portal", (id) -> SoundEvent.createVariableRangeEvent(id));

    private static final Identifier DEFAULT_TRIGGER_SOUND = PackCompanion.id("rtp_portal");
    private static volatile Identifier triggerSound = DEFAULT_TRIGGER_SOUND;

    public RtpPortalFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        modEventBus.addListener(this::addToCreativeTab);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
    }

    public static void playTriggerSound(Level level, BlockPos pos) {
        var sounds = level.registryAccess().lookupOrThrow(Registries.SOUND_EVENT);

        Identifier soundId = triggerSound;
        Holder<SoundEvent> sound = sounds.get(ResourceKey.create(Registries.SOUND_EVENT, soundId))
                .<Holder<SoundEvent>>map(holder -> holder)
                .orElseGet(() -> Holder.direct(SoundEvent.createVariableRangeEvent(soundId)));

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)) {
            event.accept(new ItemStack(RTP_PORTAL_ITEM.get()));
        }
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RtpPortalBlock.clearPlayerState(event.getEntity().getUUID());
    }

    @Override
    public void onReload(ResourceManager resourceManager) {
        triggerSound = DEFAULT_TRIGGER_SOUND;

        resourceManager.getResource(PackCompanion.id("rtp_portal.json")).ifPresent(resource -> {
            try (var reader = resource.openAsReader()) {
                var json = JsonParser.parseReader(reader);
                RtpPortalSettings.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent(settings -> triggerSound = settings.sound());
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Failed to read rtp_portal settings, using the default sound", e);
            }
        });
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        collector.translationCollector().add("block.ftbpc.rtp_portal", "Random Portal");
        collector.translationCollector().add("item.ftbpc.rtp_portal", "Random Portal");
    }
}
