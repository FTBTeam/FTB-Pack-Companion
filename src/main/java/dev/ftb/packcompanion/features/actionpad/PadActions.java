package dev.ftb.packcompanion.features.actionpad;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.value.AbstractListValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.packcompanion.PackCompanion;
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
    private static final List<PadAction> DEFAULTS = List.of(
            new PadAction("ftbpackcompanion.spawn", Icons.GLOBE, Optional.empty(), Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/spawn", PermissionLevel.GAMEMASTERS.id(), false
            )), Optional.empty(), true),
            new PadAction("ftbpackcompanion.home", Icons.COMPASS, Optional.empty(), Optional.empty(), Optional.of(new PadAction.CommandAction(
                    "/home", PermissionLevel.GAMEMASTERS.id(), false
            )), Optional.empty(), true)
    );

    private static final Config CONFIG = Config.create(PackCompanion.MOD_ID + "-pad-actions");

    public static final AbstractListValue<PadAction> ACTIONS = CONFIG.add(new AbstractListValue<PadAction>(CONFIG, "actions", DEFAULTS, PadAction.CODEC) {})
            .comment("""
                    The actions that should be available in the action pad.
                    Each action can be unlocked based on the players stages, or team stages, and can run a command or teleport the player when clicked.
                    
                    Example:
                    {
                        // This can be a translation string
                        name: "Example",
                        icon: "minecraft:diamond", // This is any supported FTB Library icon type
                        unlockedAt: "example_stage", // Optional stage that unlocks this action when completed
                        teamUnlockedAt: "example_team_stage", // Optional stage that unlocks this action when completed for the players team.

                        // You can only use one of these actions, not both!
                        commandAction: { // This action runs a command when clicked
                            command: "/example_command",
                            executionLevel: 2, // The required permission level to run the command, default 2 (game master)
                            executeAsServer: false // Whether the command should be run as the server (true) or the player (false, default)
                        },

                        teleportAction: { // This action teleports the player when clicked
                            dimension: "minecraft:overworld", // The dimension to teleport the player to
                            position: [0, 64, 0], // The position to teleport the player to
                            rotation: [0, 0] // Optional rotation to set the players view to after teleporting
                        },
                    }
                    """);

    public static void register() {
        ConfigManager.getInstance().registerServerConfig(CONFIG, PackCompanion.MOD_ID + ".pad-actions", false);
    }

    public static List<PadAction> getUnlockedActions(ServerPlayer player) {
        List<PadAction> unlocked = new ArrayList<>();
        for (PadAction action : ACTIONS.get()) {
            // If no stage condition is required, always unlocked
            if (hasUnlocked(action, player)) {
                unlocked.add(action);
                continue;
            }
        }
        return unlocked;
    }

    public static boolean hasUnlocked(PadAction action, ServerPlayer player) {
        if (action.unlockedAt().isEmpty() && action.teamUnlockedAt().isEmpty()) {
            return true;
        }

        // If the teams stage is present, check that first
        if (action.teamUnlockedAt().isPresent()) {
            if (TeamsIntegration.get().hasStage(player, action.teamUnlockedAt().get())) {
                return true;
            }
        }

        // Finally, check the normal stage condition
        // Really, the dev should never enable both as that's messy but we don't enforce that
        if (action.unlockedAt().isPresent()) {
            return StageHelper.getInstance().getProvider().has(player, action.unlockedAt().get());
        }

        return false;
    }

    public static Optional<PadAction> getAction(Player player, String actionName) {
        return getUnlockedActions((ServerPlayer) player).stream().filter(a -> a.name().equals(actionName)).findFirst();
    }
}
