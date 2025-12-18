package dev.ftb.packcompanion.integrations.teams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import net.minecraft.server.level.ServerPlayer;

public class TeamsProvider implements ITeams {
    @Override
    public boolean hasStage(ServerPlayer player, String stage) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .map(e -> TeamStagesHelper.hasTeamStage(e, stage))
                .orElse(false);
    }
}
