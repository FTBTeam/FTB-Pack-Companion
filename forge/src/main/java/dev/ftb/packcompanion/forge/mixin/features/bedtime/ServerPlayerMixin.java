package dev.ftb.packcompanion.forge.mixin.features.bedtime;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
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
    public ServerPlayerMixin(Level arg, BlockPos arg2, float f, GameProfile gameProfile) {
        super(arg, arg2, f, gameProfile);
    }

    @Shadow protected abstract boolean bedInRange(BlockPos blockPos, Direction direction);
    @Shadow public abstract @NotNull ServerLevel getLevel();
    @Shadow protected abstract boolean bedBlocked(BlockPos blockPos, Direction direction);
    @Shadow public abstract void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2);
    @Shadow public abstract boolean isCreative();
    @Shadow public abstract void displayClientMessage(Component component, boolean bl);

    @Inject(method = "startSleepInBed", at = @At(value = "RETURN"))
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> callback) {
        Either<Player.BedSleepingProblem, Unit> returnValue = callback.getReturnValue();
        Optional<BlockPos> optAt = Optional.of(blockPos);

        Direction direction = this.getLevel().getBlockState(blockPos).getValue(HorizontalDirectionalBlock.FACING);
        if (returnValue.left().isPresent() && returnValue.left().get() == Player.BedSleepingProblem.NOT_POSSIBLE_HERE) {
            if (!this.bedInRange(blockPos, direction)) {
                return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
            }
            if (this.bedBlocked(blockPos, direction)) {
                return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
            }

            this.setRespawnPosition(this.getLevel().dimension(), blockPos, this.getYRot(), false, true);
            if (!ForgeEventFactory.fireSleepingTimeCheck((ServerPlayer) (Object) this, optAt)) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
            if (!this.isCreative()) {
                Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                List<Monster> list = this.getLevel().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0), arg -> arg.isPreventingPlayerRest(this));
                if (!list.isEmpty()) {
                    return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                }
            }
            Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(blockPos).ifRight(arg -> {
                this.awardStat(Stats.SLEEP_IN_BED);
                CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayer) (Object)this);
            });
            if (!this.getLevel().canSleepThroughNights()) {
                this.displayClientMessage(new TranslatableComponent("sleep.not_possible"), true);
            }

            this.getLevel().updateSleepingPlayerList();
            return either;
        }

        return returnValue;
    }
}
