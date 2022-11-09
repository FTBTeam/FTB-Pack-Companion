package dev.ftb.packcompanion.config;

import com.google.gson.Gson;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import it.unimi.dsi.fastutil.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final int VERSION = 1;

    private static final Path CONFIG_DIR = PackCompanionExpectPlatform.getConfigDirectory();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("ftbpc-common.json");

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
        Pair<ConfigSpec, Boolean> configData = Pair.of(createDefaultConfig(), false);
        try {
            configData = this.loadData();
        } catch (IOException e) {
            LOGGER.error("Unable to read config data, defaulting to default config...", e);
        }

        loaded = true;
        this.data = configData.left();

        if (configData.right()) {
            var json = new Gson().newBuilder().setPrettyPrinting().create();
            try {
                Files.writeString(CONFIG_FILE, json.toJson(configData.left()));
            } catch (IOException e) {
                LOGGER.error("Failed to write config file", e);
            }
        }
    }

    private Pair<ConfigSpec, Boolean> loadData() throws IOException {
        if (!Files.exists(CONFIG_FILE)) {
            var defaultConfig = createDefaultConfig();
            var json = new Gson().newBuilder().setPrettyPrinting().create();
            Files.writeString(CONFIG_FILE, json.toJson(defaultConfig));

            return Pair.of(defaultConfig, false);
        }

        var configData = Files.readString(CONFIG_FILE);
        ConfigSpec configSpec = new Gson().fromJson(configData, ConfigSpec.class);
        LOGGER.info("Successfully read config data from {}", CONFIG_FILE);

        boolean updated = false;

        // Hacky but it works for now
        if (configSpec.featureBeds == null || configSpec.featureToast == null) {
            ConfigSpec defaultConfig = createDefaultConfig();
            configSpec.featureBeds = configSpec.featureBeds == null ? defaultConfig.featureBeds : configSpec.featureBeds;
            configSpec.featureToast = configSpec.featureToast == null ? defaultConfig.featureToast : configSpec.featureToast;

            updated = true;
        }

        if (configSpec.version != VERSION) {
            // Migrate
            configSpec = this.migrateConfigToNewSpec(CONFIG_FILE, configSpec);
        }

        return Pair.of(configSpec, updated);
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
        configSpec.featureBeds = ConfigSpec.FeatureConfig.of(false, "Forces beds to allows all you to sleep regardless of dimension");

        return configSpec;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
