package dev.ftb.packcompanion.neoforge;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import dev.ftb.packcompanion.neoforge.integrations.IntegrationsForge;
import net.neoforged.fml.common.Mod;

@Mod(PackCompanionAPI.MOD_ID)
public class PackCompanionForge {
    static final IntegrationsForge integrationsEntry = new IntegrationsForge();

    public PackCompanionForge() {
        PackCompanion.init();
    }
}
