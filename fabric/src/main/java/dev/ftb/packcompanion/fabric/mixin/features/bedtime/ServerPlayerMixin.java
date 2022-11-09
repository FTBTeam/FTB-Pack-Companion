package dev.ftb.packcompanion.fabric.mixin.features.bedtime;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import dev.ftb.packcompanion.config.Config;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
        super(level, blockPos, f, gameProfile, profilePublicKey);
    }

    @Shadow protected abstract boolean bedInRange(BlockPos blockPos, Direction direction);
    @Shadow public abstract @NotNull ServerLevel getLevel();
    @Shadow protected abstract boolean bedBlocked(BlockPos blockPos, Direction direction);
    @Shadow public abstract void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2);
    @Shadow public abstract boolean isCreative();
    @Shadow public abstract void displayClientMessage(Component component, boolean bl);

    @Inject(method = "startSleepInBed", at = @At(value = "RETURN"), cancellable = true)
    public void startSleepInBed(BlockPos blockPos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> callback) {
        if (!Config.get().featureBeds.enabled) {
            callback.setReturnValue(callback.getReturnValue());
            return;
        }

        var returnValue = callback.getReturnValue();

        Direction direction = this.getLevel().getBlockState(blockPos).getValue(HorizontalDirectionalBlock.FACING);
        if (returnValue.left().isPresent() && returnValue.left().get() == BedSleepingProblem.NOT_POSSIBLE_HERE) {
            if (!this.bedInRange(blockPos, direction)) {
                callback.setReturnValue(Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY));
            } else if (this.bedBlocked(blockPos, direction)) {
                callback.setReturnValue(Either.left(Player.BedSleepingProblem.OBSTRUCTED));
            } else {
                this.setRespawnPosition(this.level.dimension(), blockPos, this.getYRot(), false, true);
                if (this.level.isDay()) {
                    callback.setReturnValue(Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW));
                } else {
                    if (!this.isCreative()) {
                        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                        List<Monster> list = this.level.getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0), (monster) -> monster.isPreventingPlayerRest(this));
                        if (!list.isEmpty()) {
                            callback.setReturnValue(Either.left(Player.BedSleepingProblem.NOT_SAFE));
                            return;
                        }
                    }

                    Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(blockPos).ifRight((unit) -> {
                        this.awardStat(Stats.SLEEP_IN_BED);
                        CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayer) (Object)this);
                    });
                    if (!this.getLevel().canSleepThroughNights()) {
                        this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
                    }

                    ((ServerLevel)this.level).updateSleepingPlayerList();
                    callback.setReturnValue(either);
                }
            }
        }

        callback.setReturnValue(callback.getReturnValue());
    }
}
