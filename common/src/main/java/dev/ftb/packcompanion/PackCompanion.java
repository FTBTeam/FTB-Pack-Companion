package dev.ftb.packcompanion;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.packcompanion.config.Config;
import dev.ftb.packcompanion.registry.LootTableRegistries;
import dev.ftb.packcompanion.registry.ReloadResourceManager;
import dev.ftb.packcompanion.registry.StructureProcessorRegistry;
import net.minecraft.server.packs.PackType;

public class PackCompanion {
    public static final String MOD_ID = "ftbpc";

    public static void init() {
        Config.init();

        // Registry
        LootTableRegistries.REGISTRY.register();
        StructureProcessorRegistry.REGISTRY.register();

        // Reload listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA, ReloadResourceManager.INSTANCE);

        CommandRegistrationEvent.EVENT.register(CommandRegistry::setup);
    }
}
