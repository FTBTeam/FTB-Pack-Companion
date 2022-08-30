package dev.ftb.packcompanion.config;

import java.util.Arrays;
import java.util.List;

public class ConfigSpec {
    /**
     * Used for version updating
     */
    public int version;

    public FeatureConfig featureToast;

    public static class FeatureConfig {
        public List<String> comments;
        public boolean enabled;

        public static FeatureConfig of(boolean enabled, String... comments) {
            FeatureConfig featureConfig = new FeatureConfig();
            featureConfig.enabled = enabled;
            featureConfig.comments = Arrays.asList(comments);
            return featureConfig;
        }
    }
}
