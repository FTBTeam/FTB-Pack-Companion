package dev.ftb.packcompanion.config;

import com.google.gson.Gson;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final int VERSION = 1;

    private static Config INSTANCE;
    public static boolean loaded = false;

    public ConfigSpec data;

    public static ConfigSpec get() {
        return getOrCreate().data;
    }

    private static Config getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }

        return INSTANCE;
    }

    public static void init() {
        // Load instantly
        getOrCreate();
    }

    public Config() {
        ConfigSpec configData = createDefaultConfig();
        try {
            configData = this.loadData();
        } catch (IOException e) {
            LOGGER.error("Unable to read config data, defaulting to default config...", e);
        }

        loaded = true;
        this.data = configData;
    }

    private ConfigSpec loadData() throws IOException {
        Path configDirectory = PackCompanionExpectPlatform.getConfigDirectory();
        Path configFile = configDirectory.resolve("ftbpc-common.json");

        if (!Files.exists(configFile)) {
            var defaultConfig = createDefaultConfig();
            var json = new Gson().newBuilder().setPrettyPrinting().create();
            Files.writeString(configFile, json.toJson(defaultConfig));

            return defaultConfig;
        }

        var configData = Files.readString(configFile);
        ConfigSpec configSpec = new Gson().fromJson(configData, ConfigSpec.class);
        LOGGER.info("Successfully read config data from {}", configFile);

        if (configSpec.version != VERSION) {
            // Migrate
            configSpec = this.migrateConfigToNewSpec(configFile, configSpec);
        }

        return configSpec;
    }

    // For now, let's just yeet the old one
    private ConfigSpec migrateConfigToNewSpec(Path configFile, ConfigSpec configSpec) throws IOException {
        var defaultConfig = this.createDefaultConfig();
        var json = new Gson().newBuilder().setPrettyPrinting().create();

        Files.move(configFile, configFile.getParent().resolve("ftbpc-common.json.old"));
        Files.writeString(configFile, json.toJson(defaultConfig));

        return defaultConfig;
    }

    /**
     * This is meh
     */
    private ConfigSpec createDefaultConfig() {
        ConfigSpec configSpec = new ConfigSpec();

        configSpec.version = 1;
        configSpec.featureToast = ConfigSpec.FeatureConfig.of(true, "Allows you to completely disable the Tutorial Toasts", "Enabled by default");

        return configSpec;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
