package dev.ftb.packcompanion.features.forcedgamemodes;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ForcedGameModesFeature extends Feature.Common {
    public ForcedGameModesFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::onPlayerChangeDimension);
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translationCollector = collector.translationCollector();

        translationCollector.add("ftbpackcompanion.command.bypass.no-players", "No players are currently in the forced gamemode bypass list.");
        translationCollector.add("ftbpackcompanion.command.bypass.added", "Added %s to the forced gamemode bypass list.");
        translationCollector.add("ftbpackcompanion.command.bypass.removed", "Removed %s from the forced gamemode bypass list.");
        translationCollector.add("ftbpackcompanion.command.fixed", "Fixed gamemode for player %s.");
    }

    @Override
    public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        return List.of(Commands.literal("forcedgamemodes")
            .then(Commands.literal("fixme")
                    .executes(this::fixPlayer)
            )
            .then(Commands.literal("bypass")
                    .requires(c -> c.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("list")
                            .executes(this::listBypassPlayers)
                    )
                    .then(Commands.literal("add")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes(context -> updateBypassList(context, true))
                            )
                    )
                    .then(Commands.literal("remove")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes(context -> updateBypassList(context, false))
                            )
                    )
            )
        );
    }

    private int listBypassPlayers(CommandContext<CommandSourceStack> context) {
        Set<GameProfile> bypassPlayers = data(context.getSource().getServer()).bypassPlayers();
        if (bypassPlayers.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("ftbpackcompanion.command.bypass.no-players"), false);
            return 0;
        }

        for (GameProfile profile : bypassPlayers) {
            context.getSource().sendSuccess(() ->
                    Component.literal("- " + profile.getName() + " ")
                            .append(Component.literal("[Remove]").withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.RED)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ftbpc forcedgamemodes bypass remove " + profile.getName()))
                            ))
            , false);
        }

        return 0;
    }

    /**
     * Attempt to fix the player's gamemode based on their current dimension
     */
    private int fixPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var dimension = player.level().dimension();

        updateGameMode(player, dimension);
        context.getSource().sendSuccess(() -> Component.translatable("ftbpackcompanion.command.fixed", player.getName().getString()), false);
        return 0;
    }

    private void onPlayerChangeDimension(EntityTravelToDimensionEvent event) {
        Map<String, GameType> config = PCServerConfig.DIMENSION_FORCED_GAMEMODES.get();
        if (config.isEmpty()) {
            return;
        }

        var entity = event.getEntity();
        if (!entity.isAlive() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        // Don't change peoples gamemode if they are in the bypass list
        if (data(player.server).bypassPlayers().contains(player.getGameProfile())) {
            return;
        }

        updateGameMode(player, event.getDimension());
    }

    private void updateGameMode(ServerPlayer player, ResourceKey<Level> dimension) {
        // We don't care about spectators
        if (player.isSpectator()) {
            return;
        }

        Map<String, GameType> config = PCServerConfig.DIMENSION_FORCED_GAMEMODES.get();
        ForcedGameModeData data = data(player.server);
        var forcedGameMode = config.get(dimension.location().toString());
        if (forcedGameMode == null) {
            // Push the player back to their original gamemode if they are not in a forced gamemode dimension
            var previousGameMode = data.removePreviousGameMode(player.getUUID());
            player.setGameMode(Objects.requireNonNullElse(previousGameMode, GameType.SURVIVAL));
            return;
        }

        // Store the players previous gamemode
        data.setPreviousGameMode(player.getUUID(), player.gameMode.getGameModeForPlayer());
        player.setGameMode(forcedGameMode);
    }

    private int updateBypassList(CommandContext<CommandSourceStack> context, boolean add) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        if (add) {
            data(context.getSource().getServer()).addBypassPlayer(player.getGameProfile());
            context.getSource().sendSuccess(() -> Component.translatable("ftbpackcompanion.command.bypass.added", player.getName().getString()), false);
        } else {
            data(context.getSource().getServer()).removeBypassPlayer(player.getGameProfile());
            context.getSource().sendSuccess(() -> Component.translatable("ftbpackcompanion.command.bypass.removed", player.getName().getString()), false);
        }
        return 0;
    }

    private ForcedGameModeData data(MinecraftServer server) {
        return ForcedGameModeData.getInstance(server);
    }
}
