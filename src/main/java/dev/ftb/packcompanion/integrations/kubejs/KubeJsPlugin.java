package dev.ftb.packcompanion.integrations.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class KubeJsPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("ftb", new FTBBinding());
    }
}
