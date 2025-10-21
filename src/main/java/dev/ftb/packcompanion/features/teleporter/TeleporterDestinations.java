package dev.ftb.packcompanion.features.teleporter;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeleporterDestinations {
    private static TeleporterDestinations INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleporterDestinations.class);
    private static final List<TeleporterAction> defaultDestination = List.of(
            new TeleporterAction("ftbpackcompanion.spawn", Icons.GLOBE, Optional.empty(), Optional.of(new TeleporterAction.CommandAction(
                    "/spawn", Commands.LEVEL_GAMEMASTERS, false
            )), Optional.empty()),
            new TeleporterAction("ftbpackcompanion.home", Icons.COMPASS, Optional.empty(), Optional.of(new TeleporterAction.CommandAction(
                    "/home", Commands.LEVEL_GAMEMASTERS, false
            )), Optional.empty())
    );

    public static TeleporterDestinations get() {
        if (INSTANCE == null) {
            INSTANCE = new TeleporterDestinations();
            INSTANCE.load();
        }

        return INSTANCE;
    }

    private final List<TeleporterAction> destinations = new ArrayList<>();
    private final Path destinationsFile = FMLPaths.CONFIGDIR.get().resolve("ftbpc_server_destinations.snbt");

    public void load() {
        try {
            var compoundTag = SNBT.tryRead(destinationsFile);
            if (compoundTag != null) {
                destinations.clear();
                TeleporterAction.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.getList("destinations", CompoundTag.TAG_COMPOUND))
                        .result()
                        .ifPresentOrElse(
                                destinations::addAll,
                                () -> destinations.addAll(defaultDestination)
                        );
            }
        } catch (Exception error) {
            LOGGER.error("Failed to load teleporter destinations, using default", error);
        } finally {
            if (destinations.isEmpty()) {
                destinations.addAll(defaultDestination);
                // Try and write the default file
                writeDefault();
            }
        }
    }

    public void writeDefault() {
        ListTag items = TeleporterAction.CODEC.listOf().encodeStart(NbtOps.INSTANCE, defaultDestination)
                .result()
                .map(nbt -> (ListTag) nbt)
                .orElse(new ListTag());

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("destinations", items);

        try {
            SNBT.tryWrite(destinationsFile, compoundTag);
        } catch (Exception error) {
            LOGGER.error("Failed to write default teleporter destinations", error);
        }
    }

    public List<TeleporterAction> getUnlockedDestinations(Player player) {
        List<TeleporterAction> unlocked = new ArrayList<>();
        for (TeleporterAction action : destinations) {
            if (action.unlockedAt().isEmpty()) {
                unlocked.add(action);
                continue;
            }

            if (StageHelper.getInstance().getProvider().has(player, action.unlockedAt().get())) {
                unlocked.add(action);
            }
        }
        return unlocked;
    }

    public List<TeleporterAction> getDestinations() {
        return destinations;
    }
}
