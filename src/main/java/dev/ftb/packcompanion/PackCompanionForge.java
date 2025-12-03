package dev.ftb.packcompanion.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.forge.integrations.IntegrationsForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PackCompanionAPI.MOD_ID)
public class PackCompanionForge {
    static final IntegrationsForge integrationsEntry = new IntegrationsForge();

    public PackCompanionForge() {
        EventBuses.registerModEventBus(PackCompanionAPI.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PackCompanion.init();

    }
}
