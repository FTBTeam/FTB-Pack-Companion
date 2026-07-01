package dev.ftb.packcompanion.features.kube;

import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;

public class KubeStd {
    /**
     * {@link net.minecraft.resources.ResourceLocation} utility functions
     */
    public Id id = new Id();

    /**
     * General utility functions
     */
    public Utils utils = new Utils();

    /**
     * String utility functions
     */
    public Strings str = new Strings();

    /**
     * Returns the value if it is set, otherwise returns the default value
     */
    public <T> T getOrDefault(T value, T defaultValue) {
        return isUnset(value) ? defaultValue : value;
    }

    /**
     * Checks for a javascript unset value, null or undefined
     */
    public boolean isUnset(Object value) {
        return value == null || value instanceof Undefined || value == Scriptable.NOT_FOUND;
    }

    /**
     * Checks for a javascript set value, not null or undefined
     */
    public boolean isSet(Object value) {
        return !isUnset(value);
    }
}
