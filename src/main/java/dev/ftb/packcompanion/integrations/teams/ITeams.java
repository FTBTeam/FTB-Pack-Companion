package dev.ftb.packcompanion.integrations.teams;

import net.minecraft.server.level.ServerPlayer;

public interface ITeams {
    boolean hasStage(ServerPlayer player, String stage);
}
