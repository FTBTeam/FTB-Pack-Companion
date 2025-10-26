package dev.ftb.packcompanion.features.actionpad;

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

public class PadActions {
    private static PadActions INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(PadActions.class);
    private static final List<PadAction> defaultDestination = List.of(
            new PadAction("ftbpackcompanion.spawn", Icons.GLOBE, Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/spawn", Commands.LEVEL_GAMEMASTERS, false
            )), Optional.empty(), true),
            new PadAction("ftbpackcompanion.home", Icons.COMPASS, Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/home", Commands.LEVEL_GAMEMASTERS, false
            )), Optional.empty(), true)
    );

    public static PadActions get() {
        if (INSTANCE == null) {
            INSTANCE = new PadActions();
            INSTANCE.load();
        }

        return INSTANCE;
    }

    private final List<PadAction> actions = new ArrayList<>();
    private final Path destinationsFile = FMLPaths.CONFIGDIR.get().resolve("ftbpc_pad_actions.snbt");

    public void load() {
        try {
            var compoundTag = SNBT.tryRead(destinationsFile);
            if (compoundTag != null) {
                actions.clear();
                PadAction.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.getList("actions", CompoundTag.TAG_COMPOUND))
                        .result()
                        .ifPresentOrElse(
                                actions::addAll,
                                () -> actions.addAll(defaultDestination)
                        );
            }
        } catch (Exception error) {
            LOGGER.error("Failed to load action pad actions, using default", error);
        } finally {
            if (actions.isEmpty()) {
                actions.addAll(defaultDestination);
                // Try and write the default file
                writeDefault();
            }
        }
    }

    public void writeDefault() {
        ListTag items = PadAction.CODEC.listOf().encodeStart(NbtOps.INSTANCE, defaultDestination)
                .result()
                .map(nbt -> (ListTag) nbt)
                .orElse(new ListTag());

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("actions", items);

        try {
            SNBT.tryWrite(destinationsFile, compoundTag);
        } catch (Exception error) {
            LOGGER.error("Failed to write default action pad actions", error);
        }
    }

    public List<PadAction> getUnlockedActions(Player player) {
        List<PadAction> unlocked = new ArrayList<>();
        for (PadAction action : actions) {
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

    public Optional<PadAction> getAction(Player player, String actionName) {
        return getUnlockedActions(player).stream().filter(a -> a.name().equals(actionName)).findFirst();
    }
}
