package dev.ftb.packcompanion.mixin.features.commandhistory;

import dev.ftb.packcompanion.features.commandhistory.CommandHistoryFeature;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Shadow
    @Final
    private List<String> recentChat;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ftbpc$init(CallbackInfo ci) {
        CommandHistoryFeature.get().ifPresent(e -> recentChat.addAll(e.history()));
    }

    @Inject(method = "addRecentChat", at = @At("TAIL"))
    private void ftbpc$addRecentChat(String s, CallbackInfo ci) {
        if (s.startsWith("/")) {
            CommandHistoryFeature.get().ifPresent(e -> e.addCommand(s));
        }
    }

    @Inject(method = "clearMessages", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void ftbpc$clearMessages(boolean clearSentMsgHistory, CallbackInfo ci) {
        if (clearSentMsgHistory) {
            CommandHistoryFeature.get().ifPresent(e -> recentChat.addAll(e.history()));
        }
    }
}
