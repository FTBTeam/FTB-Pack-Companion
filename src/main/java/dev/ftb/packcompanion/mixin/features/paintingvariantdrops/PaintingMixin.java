package dev.ftb.packcompanion.mixin.features.paintingvariantdrops;

import dev.ftb.packcompanion.PackCompanionTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Painting.class)
public abstract class PaintingMixin extends HangingEntity {
    protected PaintingMixin(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/painting/Painting;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), cancellable = true)
    public void dropItem(ServerLevel level, Entity causedBy, CallbackInfo ci) {
        Painting painting = (Painting) (Object) this;
        Holder<PaintingVariant> variant = painting.getVariant();

        if (variant.is(PackCompanionTags.Painting.DROPS_WITH_VARIANT)) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString(Entity.TAG_ID, BuiltInRegistries.ENTITY_TYPE.getKey(getType()).toString());
            PaintingVariant.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), variant)
                    .ifSuccess((tag) -> compoundTag.merge((CompoundTag) tag));

            ItemStack itemStack = new ItemStack(Items.PAINTING);
            itemStack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(getType(), compoundTag));

            this.spawnAtLocation(level, itemStack);
            ci.cancel();
        }
    }
}
