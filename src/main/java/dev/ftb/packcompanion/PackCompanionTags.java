package dev.ftb.packcompanion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public class PackCompanionTags {
    public static class Painting {
        public static final TagKey<PaintingVariant> DROPS_WITH_VARIANT = modTag("drops_with_variant");

        static TagKey<PaintingVariant> tag(String modid, String name) {
            return TagKey.create(Registries.PAINTING_VARIANT, Identifier.fromNamespaceAndPath(modid, name));
        }

        static TagKey<PaintingVariant> modTag(String name) {
            return tag(PackCompanion.MOD_ID, name);
        }
    }
}
