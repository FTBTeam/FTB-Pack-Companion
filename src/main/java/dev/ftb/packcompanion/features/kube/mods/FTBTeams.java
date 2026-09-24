package dev.ftb.packcompanion.features.kube.mods;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FTBTeams {
    public static final FTBTeams INSTANCE = new FTBTeams();

    public TeamManager manager() {
        return FTBTeamsAPI.api().getManager();
    }

    public ClientTeamManager clientManager() {
        return FTBTeamsAPI.api().getClientManager();
    }

    public UUID teamIdFromPlayer(ServerPlayer player) {
        return manager().getTeamForPlayer(player)
                .map(Team::getId)
                .orElse(null);
    }

    public UUID teamIdFromPlayerId(UUID playerId) {
        return manager().getTeamByID(playerId)
                .map(Team::getId)
                .orElse(null);
    }

    public Team teamForPlayer(ServerPlayer player) {
        return manager().getTeamByID(player.getUUID())
                .orElse(null);
    }

    public CompoundTag persistentData(ServerPlayer player) {
        return manager().getTeamForPlayer(player).map(Team::getExtraData).orElse(null);
    }

    public String shortTeamName(Team team) {
        return team.getShortName();
    }

    public String shortTeamNameFromPlayer(ServerPlayer player) {
        return manager().getTeamForPlayer(player).map(Team::getShortName).orElse(null);
    }

    public Team teamById(UUID teamId) {
        return manager().getTeamByID(teamId).orElse(null);
    }
}
