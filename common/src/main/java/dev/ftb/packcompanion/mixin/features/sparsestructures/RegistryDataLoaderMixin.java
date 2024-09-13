package dev.ftb.packcompanion.mixin.features.sparsestructures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Decoder;
import dev.ftb.packcompanion.config.PCCommonConfig;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {
    @Inject(
            method = "loadElementFromResource",
            at = @At(
                    value = "INVOKE",
                    remap = false,
                    target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
            )
    )
    private static <E> void ssr$init(WritableRegistry<E> writableRegistry, Decoder<E> decoder, RegistryOps<JsonElement> registryOps, ResourceKey<E> resourceKey, Resource resource, RegistrationInfo registrationInfo, CallbackInfo ci, @Local JsonElement jsonElement) {
        if (!PCCommonConfig.SPARSE_STRUCTURES.get().enabled()) {
            return;
        }

        String string = Registries.elementsDirPath(writableRegistry.key());
        PCCommonConfig.SparseStructuresConfig config = PCCommonConfig.SPARSE_STRUCTURES.get();

        if (string.equals("worldgen/structure_set")) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject placement = jsonObject.getAsJsonObject("placement");
            if (placement.get("type").getAsString().equals("minecraft:concentric_rings")) return;

            double factor = config.customSpreadFactors()
                    .stream()
                    .filter(s -> {
                        if (s == null) return false;

                        String structure_set = resourceKey.location().toString();
                        return structure_set.equals(s.structure()) || jsonObject.getAsJsonArray("structures").asList().stream().anyMatch(p -> p.getAsJsonObject().get("structure").getAsString().equals(s.structure()));
                    })
                    .findFirst()
                    .orElse(new PCCommonConfig.CustomSpreadFactors("", config.globalSpreadFactor())).spreadFactor();

            int spacing;
            int separation;

            if (placement.get("spacing") == null) {
                spacing = 1;
            } else {
                spacing = (int) (Math.min(placement.get("spacing").getAsDouble() * factor, 4096.0));
            }

            if (placement.get("separation") == null) {
                separation = 1;
            } else {
                separation = (int) (Math.min(placement.get("separation").getAsDouble() * factor, 4096.0));
            }

            if (separation >= spacing) {
                if (spacing == 0) {
                    spacing = 1;
                }

                separation = spacing - 1;
            }

            placement.addProperty("spacing", spacing);
            placement.addProperty("separation", separation);
        }
    }
}
