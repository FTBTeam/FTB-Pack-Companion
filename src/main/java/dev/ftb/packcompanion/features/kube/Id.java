package dev.ftb.packcompanion.features.kube;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class Id {
    /**
     * Create a standard {@link Identifier} from a namespace and path
     */
    public Identifier create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Attempt to parse a string into a {@link Identifier}, returning null if the string is invalid
     */
    @Nullable
    public Identifier parseOrNull(String id) {
        return Identifier.tryParse(id);
    }

    /**
     * Create a standard {@link Identifier} in the "ftb" namespace
     */
    public Identifier ftb(String path) {
        return Identifier.fromNamespaceAndPath("ftb", path);
    }

    /**
     * Create a standard {@link Identifier} in the "minecraft" namespace
     */
    public Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    /**
     * Create a standard {@link Identifier} in the "kubejs" namespace
     */
    public Identifier kube(String path) {
        return Identifier.fromNamespaceAndPath("kubejs", path);
    }
}
