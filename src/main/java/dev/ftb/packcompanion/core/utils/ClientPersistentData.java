package dev.ftb.packcompanion.core.utils;

import dev.ftb.mods.ftblibrary.config.serializer.Json5ConfigSerializer;
import dev.ftb.mods.ftblibrary.config.value.Config;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class ClientPersistentData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPersistentData.class);

    private final String name;
    private final Path path;

    private final Config backingData;

    public ClientPersistentData(String name) {
        this.name = name;
        this.path = FMLPaths.GAMEDIR.get()
                .resolve("local")
                .resolve("ftbpackcompanion")
                .resolve(name + ".json5");

        this.backingData = Config.create(this.name);
    }

    public Config data() {
        return backingData;
    }

    public void save() {
        try {
            Json5ConfigSerializer.writeToFile(backingData, path);
        } catch (IOException e) {
            LOGGER.error("Failed to save client persistent data '{}'", name, e);
        }
    }

    public void load() {
        try {
            Json5ConfigSerializer.readFromFile(backingData, path);
        } catch (IOException e) {
            LOGGER.error("Failed to load client persistent data '{}'", name, e);
        }
    }
}
