package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.packcompanion.integrations.teams.TeamsIntegration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PadActions {
    @Nullable
    private static PadActions INSTANCE = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(PadActions.class);
    private static final List<PadAction> defaultDestination = List.of(
            new PadAction("ftbpackcompanion.spawn", Icons.GLOBE, Optional.empty(), Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/spawn", PermissionLevel.GAMEMASTERS.id(), false
            )), Optional.empty(), true),
            new PadAction("ftbpackcompanion.home", Icons.COMPASS, Optional.empty(), Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/home", PermissionLevel.GAMEMASTERS.id(), false
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
    private final Path destinationsFile = FMLPaths.CONFIGDIR.get().resolve("ftbpc_pad_actions.json5");

    public void load() {
        try {
            if (!Files.exists(destinationsFile)) {
                writePopulatedDefaults();
                return;
            }

            Json5 json5 = new Json5();
            Json5Element parse = json5.parse(Files.readString(destinationsFile));
            actions.clear();
            PadAction.CODEC.listOf().parse(Json5Ops.INSTANCE, parse.getAsJson5Object().get("actions").getAsJson5Array())
                    .result()
                    .ifPresentOrElse(
                            actions::addAll,
                            () -> actions.addAll(defaultDestination)
                    );
        } catch (Exception error) {
            LOGGER.error("Failed to load action pad actions, using default", error);
        } finally {
            writePopulatedDefaults();
        }
    }

    private void writePopulatedDefaults() {
        if (actions.isEmpty()) {
            actions.addAll(defaultDestination);
            // Try and write the default file
            writeDefault();
        }
    }

    public void writeDefault() {
        Json5Element items = PadAction.CODEC.listOf().encodeStart(Json5Ops.INSTANCE, defaultDestination)
                .result()
                .orElse(new Json5Object());

        Json5Object obj = new Json5Object();
        obj.add("actions", items);

        try {
            Files.createDirectories(destinationsFile.getParent());
            Files.writeString(destinationsFile, new Json5().serialize(obj));
        } catch (Exception error) {
            LOGGER.error("Failed to write default action pad actions", error);
        }
    }

    public List<PadAction> getUnlockedActions(ServerPlayer player) {
        List<PadAction> unlocked = new ArrayList<>();
        for (PadAction action : actions) {
            // If no stage condition is required, always unlocked
            if (action.unlockedAt().isEmpty() && action.teamUnlockedAt().isEmpty()) {
                unlocked.add(action);
                continue;
            }

            // If the teams stage is present, check that first
            if (action.teamUnlockedAt().isPresent()) {
                if (TeamsIntegration.get().hasStage(player, action.teamUnlockedAt().get())) {
                    unlocked.add(action);
                    continue;
                }
            }

            // Finally, check the normal stage condition
            // Really, the dev should never enable both as that's messy but we don't enforce that
            if (action.unlockedAt().isPresent()) {
                if (StageHelper.getInstance().getProvider().has(player, action.unlockedAt().get())) {
                    unlocked.add(action);
                }
            }
        }
        return unlocked;
    }

    public Optional<PadAction> getAction(Player player, String actionName) {
        return getUnlockedActions((ServerPlayer) player).stream().filter(a -> a.name().equals(actionName)).findFirst();
    }
}
