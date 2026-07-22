package dev.ftb.packcompanion.features.kube.mods;

import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class FTBQuests {
    public static final FTBQuests INSTANCE = new FTBQuests();

    public boolean isCompleted(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).isCompleted(e))
                .orElse(false);
    }

    public boolean isStarted(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).isStarted(e))
                .orElse(false);
    }

    public int relativeQuestProgress(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).getRelativeProgress(e))
                .orElse(0);
    }

    public boolean isLocked(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).isLocked())
                .orElse(false);
    }

    public void pinQuest(Player player, String questId) {
        if (isPinned(player, questId)) {
            return;
        }

        getObjectById(questId)
                .ifPresent(e -> getTeamData(player).setQuestPinned(player, e.getId(), true));
    }

    public void unpinQuest(Player player, String questId) {
        if (!isPinned(player, questId)) {
            return;
        }

        getObjectById(questId)
                .ifPresent(e -> getTeamData(player).setQuestPinned(player, e.getId(), false));
    }

    public boolean isPinned(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).isQuestPinned(player, e.getId()))
                .orElse(false);
    }

    public boolean hasUnclaimedRewards(Player player, String questId) {
        return getObjectById(questId)
                .map(e -> getTeamData(player).hasUnclaimedRewards(player.getUUID(), e))
                .orElse(false);
    }

    public TeamData getTeamData(Player player) {
        var playerTeam = FTBTeams.INSTANCE.teamForPlayer((ServerPlayer) player);
        return ServerQuestFile.getInstance().getOrCreateTeamData(playerTeam);
    }

    public Optional<QuestObject> getObjectById(String id) {
        // The quest id is a hex string that we need to convert back into a long
        return QuestObjectBase.parseHexId(id)
                .map(e -> ServerQuestFile.getInstance().get(e));
    }
}
