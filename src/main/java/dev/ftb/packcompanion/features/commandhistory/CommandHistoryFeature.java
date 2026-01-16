package dev.ftb.packcompanion.features.commandhistory;

import com.google.common.base.Charsets;
import dev.ftb.packcompanion.core.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;

public class CommandHistoryFeature extends Feature.Client {
    public static CommandHistoryFeature instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHistoryFeature.class);

    private static final int MAX_PERSISTED_COMMAND_HISTORY = 100;
    private static final String COMMAND_HISTORY_FILE = "command_history.txt";

    private final Deque<String> lastCommands = new ArrayDeque<>(MAX_PERSISTED_COMMAND_HISTORY);
    private final Path commandHistoryPath;

    public CommandHistoryFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
        this.commandHistoryPath = FMLPaths.GAMEDIR.get().resolve(COMMAND_HISTORY_FILE);
        instance = this;
        load();
    }

    public void addCommand(String command) {
        if (command == null || command.isEmpty()) {
            return;
        }

        lastCommands.addLast(command);

        // Ensure we don't exceed the maximum size
        while (lastCommands.size() > MAX_PERSISTED_COMMAND_HISTORY) {
            lastCommands.removeFirst();
        }

        save();
    }

    private void load() {
        if (Files.exists(this.commandHistoryPath)) {
            try (BufferedReader bufferedreader = Files.newBufferedReader(this.commandHistoryPath, Charsets.UTF_8)) {
                this.lastCommands.addAll(bufferedreader.lines().toList());
            } catch (Exception exception) {
                LOGGER.error("Failed to read {}, command history will be missing", "command_history.txt", exception);
            }
        }
    }

    private void save() {
        try (BufferedWriter bufferedwriter = Files.newBufferedWriter(this.commandHistoryPath, Charsets.UTF_8)) {
            for (String s : this.lastCommands) {
                bufferedwriter.write(s);
                bufferedwriter.newLine();
            }
        } catch (IOException ioexception) {
            LOGGER.error("Failed to write {}, command history will be missing", "command_history.txt", ioexception);
        }
    }

    public Collection<String> history() {
        return lastCommands;
    }

    public static Optional<CommandHistoryFeature> get() {
        return Optional.ofNullable(instance);
    }
}
