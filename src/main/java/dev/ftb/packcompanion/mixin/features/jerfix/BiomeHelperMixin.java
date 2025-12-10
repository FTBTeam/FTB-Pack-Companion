package dev.ftb.packcompanion.mixin.features.jerfix;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mixin to fix JustEnoughResources biome display for modded biomes.
 * JER uses VanillaRegistries.createLookup() which only returns vanilla biomes.
 * This fix uses the current level's registry which includes modded biomes.
 * 
 * This mixin is only applied if JER is present (checked by FTBPCMixinPlugin).
 */
@Mixin(targets = "jeresources.api.util.BiomeHelper", remap = false)
public class BiomeHelperMixin {

    /**
     * Fallback to vanilla registry if any aren't available
     */
    private static List<Biome> fallbackGetAllBiomes() {
        List<Biome> biomes = new ArrayList<>();
        VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME)
                .listElements().map(Holder.Reference::value).forEach(biomes::add);
        return biomes;
    }

    private static Biome fallbackGetBiome(ResourceKey<Biome> key) {
        return VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME)
                .getOrThrow(key).value();
    }

    private static List<Biome> fallbackGetBiomes(ResourceKey<Biome> category) {
        List<Biome> biomes = new ArrayList<>();
        VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME)
                .listElements().forEach(biomeEntry -> {
                    if (biomeEntry.key().equals(category)) {
                        biomes.add(biomeEntry.value());
                    }
                });
        return biomes;
    }

    @Overwrite
    public static List<Biome> getAllBiomes() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return fallbackGetAllBiomes();
        }

        Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
        if (registry.isEmpty()) {
            return fallbackGetAllBiomes();
        }

        List<Biome> biomes = new ArrayList<>();
        registry.get().forEach(biomes::add);
        return biomes;
    }

    @Overwrite
    public static Biome getBiome(ResourceKey<Biome> key) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return fallbackGetBiome(key);
        }

        Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
        if (registry.isEmpty()) {
            return fallbackGetBiome(key);
        }

        return registry.get().get(key);
    }

    @Overwrite
    public static List<Biome> getBiomes(ResourceKey<Biome> category) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return fallbackGetBiomes(category);
        }

        Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
        if (registry.isEmpty()) {
            return fallbackGetBiomes(category);
        }

        List<Biome> biomes = new ArrayList<>();
        registry.get().asLookup().listElements().forEach(biomeEntry -> {
            if (biomeEntry.key().equals(category)) {
                biomes.add(biomeEntry.value());
            }
        });
        return biomes;
    }
}
