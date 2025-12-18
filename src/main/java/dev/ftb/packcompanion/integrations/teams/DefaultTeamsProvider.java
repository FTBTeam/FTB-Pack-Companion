package dev.ftb.packcompanion.integrations.teams;

import net.minecraft.server.level.ServerPlayer;

public class DefaultTeamsProvider implements ITeams {
    @Override
    public boolean hasStage(ServerPlayer player, String stage) {
        return false;
    }
}
