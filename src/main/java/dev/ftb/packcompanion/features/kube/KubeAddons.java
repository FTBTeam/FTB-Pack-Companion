package dev.ftb.packcompanion.features.kube;

import dev.ftb.packcompanion.features.kube.mods.FTBChunks;
import dev.ftb.packcompanion.features.kube.mods.FTBChunksClient;
import dev.ftb.packcompanion.features.kube.mods.FTBQuests;
import dev.ftb.packcompanion.features.kube.mods.FTBTeams;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import net.neoforged.fml.ModList;

import java.util.function.Supplier;

public class KubeAddons implements KubeJSPlugin {
    private static final KubeStd stdLib = new KubeStd();

    @Override
    public void registerBindings(BindingRegistry bindings) {
        System.out.println("Registering KubeJS bindings for Pack Companion");
        bindings.add("_", stdLib);

        if (!bindings.type().isClient()) {
            // TODO: Teams might be useful on the client as well but we'd need to figure that out
            bindIfLoaded(bindings, "ftbteams", () -> FTBTeams.INSTANCE);
            // TODO: Quests might also be useful on the client but we should likely have client versions for both
            bindIfLoaded(bindings, "ftbquests", () -> FTBQuests.INSTANCE);
            bindIfLoaded(bindings, "ftbchunks", () -> FTBChunks.INSTANCE);
        }

        if (bindings.type().isClient()) {
            bindIfLoaded(bindings, "ftbchunksclient", () -> FTBChunksClient.INSTANCE);
        }
    }

    private void bindIfLoaded(BindingRegistry bindings, String modId, Supplier<Object> o) {
        if (ModList.get().isLoaded(modId)) {
            bindings.add(modId, o.get());
        }
    }
}
