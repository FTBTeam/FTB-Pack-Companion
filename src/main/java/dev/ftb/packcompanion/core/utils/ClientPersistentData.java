package dev.ftb.packcompanion.core.utils;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;

import java.nio.file.Path;

public class ClientPersistentData {
    private final String name;
    private final Path path;

    private final SNBTConfig backingData;

    public ClientPersistentData(String name) {
        this.name = name;
        this.path = Platform.getGameFolder()
                .resolve("moddata")
                .resolve("ftbpackcompanion")
                .resolve(name + ".snbt");

        this.backingData = SNBTConfig.create(this.name);
    }

    public SNBTConfig data() {
        return backingData;
    }

    public void save() {
        backingData.save(path);
    }

    public void load() {
        backingData.load(path);
    }
}
