package dev.ftb.packcompanion.mixin.fixes;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

/**
 * Mixin to fix reliquified twilight forest firefly queen.
 * The firefly queen will try and place a waterlogged firefly down causing the player to crash
 * This fix just returns early if the block is water.
 *
 * The @Pseudo annotation allows this mixin to silently skip if JER is not present.
 */

@Pseudo
@Mixin(targets = "it.hurts.octostudios.reliquified_twilight_forest.item.relic.FireflyQueenItem", remap = false)
public class RTWFireflyQueenItemMixin {
    @Inject(method = "curioTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"), cancellable = true)
    public void curioTick(SlotContext slotContext, ItemStack stack, CallbackInfo ci){
        ci.cancel();
    }
}
