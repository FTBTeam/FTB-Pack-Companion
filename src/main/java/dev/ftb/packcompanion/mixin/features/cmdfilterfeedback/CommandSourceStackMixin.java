package dev.ftb.packcompanion.mixin.features.cmdfilterfeedback;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {
    @Shadow
    @Nullable
    public abstract Entity getEntity();

    @Inject(method = "broadcastToAdmins", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"), cancellable = true)
    private void ftbpc$broadcastToBlacklistedAdminsOnly(Component message, CallbackInfo ci, @Local() ServerPlayer serverplayer) {
        var sourcePlayer = this.getEntity();
        if (!(sourcePlayer instanceof ServerPlayer)) {
            return; // No-op if the command source is not a player
        }

        if (!PCServerConfig.BLACKLISTED_OPS.get().contains(sourcePlayer.getStringUUID())) {
            return; // No-op if the source player running the command is not part of the blacklist
        }

        // Should we filter the message? If so, cancel the broadcast to admins.
        if (isFilteredAdminBroadcast(message)) {
            // If the player is not a valid blacklisted op, we don't send the event from a blacklisted ops command.
            if (!PCServerConfig.BLACKLISTED_OPS.get().contains(serverplayer.getStringUUID())) {
                ci.cancel();
            }
        }
    }

    @Unique
    private static boolean isFilteredAdminBroadcast(Component message) {
        if (PCServerConfig.BLACKLISTED_OPS.get().isEmpty()) {
            return false;
        }

        if (PCServerConfig.HIDDEN_COMMAND_FEEDBACK_KEYS.get().isEmpty()) {
            return false;
        }

        if (!(message.getContents() instanceof TranslatableContents contents)) {
            return false;
        }

        return PCServerConfig.HIDDEN_COMMAND_FEEDBACK_KEYS.get()
                .stream().anyMatch(key -> contents.getKey().startsWith(key));
    }
}
