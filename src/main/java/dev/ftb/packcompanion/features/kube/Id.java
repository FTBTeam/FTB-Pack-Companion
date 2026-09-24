package dev.ftb.packcompanion.features.kube;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Id {
    /**
     * Create a standard {@link ResourceLocation} from a namespace and path
     */
    public ResourceLocation create(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Attempt to parse a string into a {@link ResourceLocation}, returning null if the string is invalid
     */
    @Nullable
    public ResourceLocation parseOrNull(String id) {
        return ResourceLocation.tryParse(id);
    }

    /**
     * Create a standard {@link ResourceLocation} in the "ftb" namespace
     */
    public ResourceLocation ftb(String path) {
        return ResourceLocation.fromNamespaceAndPath("ftb", path);
    }

    /**
     * Create a standard {@link ResourceLocation} in the "minecraft" namespace
     */
    public ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    /**
     * Create a standard {@link ResourceLocation} in the "kubejs" namespace
     */
    public ResourceLocation kube(String path) {
        return ResourceLocation.fromNamespaceAndPath("kubejs", path);
    }
}
