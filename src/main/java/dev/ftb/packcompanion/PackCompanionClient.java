package dev.ftb.packcompanion;

import dev.ftb.packcompanion.config.PCClientConfig;
import dev.ftb.packcompanion.core.Feature;
import dev.ftb.packcompanion.integrations.Integrations;
import net.minecraft.client.Minecraft;

import java.util.List;

public class PackCompanionClient {
    public static void init(List<Feature.Client> clientFeatures) {
        PCClientConfig.init();

        for (Feature.Client feature : clientFeatures) {
            feature.onClientInit();
        }

        Integrations.clientInit();

        if (PCClientConfig.DISABLE_SOCIALINTERACTION_TOASTS.get() && !Minecraft.getInstance().options.joinedFirstServer) {
            Minecraft.getInstance().options.joinedFirstServer = true;
            Minecraft.getInstance().options.save();
        }
    }
}
