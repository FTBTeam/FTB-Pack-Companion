package dev.ftb.packcompanion.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PackCompanion.MOD_ID)
public class PackCompanionForge {
    public PackCompanionForge() {
        EventBuses.registerModEventBus(PackCompanion.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PackCompanion.init();

    }
}
