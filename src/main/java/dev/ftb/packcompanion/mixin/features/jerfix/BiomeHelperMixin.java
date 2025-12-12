package dev.ftb.packcompanion.mixin.features.jerfix;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mixin to fix JustEnoughResources biome display for modded biomes.
 * JER uses VanillaRegistries.createLookup() which only returns vanilla biomes.
 * This fix uses the current level's registry which includes modded biomes.
 *
 * The @Pseudo annotation allows this mixin to silently skip if JER is not present.
 */
@Pseudo
@Mixin(targets = "jeresources.api.util.BiomeHelper", remap = false)
public class BiomeHelperMixin {

    @Inject(method = "getAllBiomes", at = @At("HEAD"), cancellable = true)
    private static void getAllBiomes(CallbackInfoReturnable<List<Biome>> cir) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
            if (registry.isPresent()) {
                List<Biome> biomes = new ArrayList<>();
                registry.get().forEach(biomes::add);
                cir.setReturnValue(biomes);
            }
        }
    }

    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private static void getBiome(ResourceKey<Biome> key, CallbackInfoReturnable<Biome> cir) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
            if (registry.isPresent()) {
                Biome biome = registry.get().get(key);
                if (biome != null) {
                    cir.setReturnValue(biome);
                }
            }
        }
    }

    @Inject(method = "getBiomes", at = @At("HEAD"), cancellable = true)
    private static void getBiomes(ResourceKey<Biome> category, CallbackInfoReturnable<List<Biome>> cir) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Optional<Registry<Biome>> registry = level.registryAccess().registry(Registries.BIOME);
            if (registry.isPresent()) {
                List<Biome> biomes = new ArrayList<>();
                registry.get().asLookup().listElements().forEach(biomeEntry -> {
                    if (biomeEntry.key().equals(category)) {
                        biomes.add(biomeEntry.value());
                    }
                });
                cir.setReturnValue(biomes);
            }
        }
    }
}
