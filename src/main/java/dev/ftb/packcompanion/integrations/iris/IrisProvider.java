package dev.ftb.packcompanion.integrations.iris;

import dev.ftb.packcompanion.config.PCCommonConfig;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.config.IrisConfig;

public class IrisProvider implements ShaderProvider {
    @Override
    public void applyShaderPack(String shaderPack) {
        IrisConfig irisConfig = Iris.getIrisConfig();
        if (!irisConfig.areShadersEnabled()) {
            String packToUse = PCCommonConfig.SHADER_PACK_TO_USE.get();
            if (!packToUse.isEmpty()) {
                irisConfig.setShaderPackName(packToUse);
            }

            IrisApi.getInstance().getConfig().setShadersEnabledAndApply(true);
        }
    }

    @Override
    public void disabledShaders() {
        IrisConfig irisConfig = Iris.getIrisConfig();
        if (irisConfig.areShadersEnabled()) {
            IrisApi.getInstance().getConfig().setShadersEnabledAndApply(false);
        }
    }
}
