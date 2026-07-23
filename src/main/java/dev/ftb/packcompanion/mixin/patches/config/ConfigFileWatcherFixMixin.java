package dev.ftb.packcompanion.mixin.patches.config;

import com.electronwill.nightconfig.core.file.FileWatcher;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(ConfigFileTypeHandler.class)
public class ConfigFileWatcherFixMixin {

    @Redirect(
            method = "lambda$reader$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/electronwill/nightconfig/core/file/FileWatcher;addWatch(Ljava/nio/file/Path;Ljava/lang/Runnable;)V"
            ),
            remap = false
    )
    private void ftbpc$skipConfigFileWatch(FileWatcher instance, Path path, Runnable callback) {
    }
}
