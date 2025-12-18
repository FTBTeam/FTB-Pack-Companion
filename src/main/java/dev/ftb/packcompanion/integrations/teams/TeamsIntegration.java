package dev.ftb.packcompanion.integrations.teams;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class TeamsIntegration implements ITeams {
    private static TeamsIntegration INSTANCE;
    private final ITeams teamsImpl;

    private TeamsIntegration(ITeams teamsImpl) {
        this.teamsImpl = teamsImpl;
    }

    public static TeamsIntegration get() {
        if (INSTANCE == null) {
            INSTANCE = new TeamsIntegration(
                    ModList.get().isLoaded("ftbteams") ?
                            new TeamsProvider() :
                            new DefaultTeamsProvider()
            );
        }

        return INSTANCE;
    }


    @Override
    public boolean hasStage(ServerPlayer player, String stage) {
        return teamsImpl.hasStage(player, stage);
    }
}
