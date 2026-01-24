package dev.ftb.packcompanion.features.forcedgamemodes;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ForcedGameModeData extends SavedData {
    private static final String DATA_NAME = "ftb_pc_forced_gamemodes";
    static final Logger LOGGER = LoggerFactory.getLogger(ForcedGameModeData.class);

    private final Set<GameProfile> bypassPlayers = new HashSet<>();
    private final Map<UUID, GameType> previousGameModes = new HashMap<>();

    public ForcedGameModeData() {
    }

    public static ForcedGameModeData getInstance(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ForcedGameModeData::new, ForcedGameModeData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)
                , DATA_NAME);
    }

    private static ForcedGameModeData load(CompoundTag tag, HolderLookup.Provider provider) {
        return new ForcedGameModeData().readNBT(tag);
    }

    public Set<GameProfile> bypassPlayers() {
        return bypassPlayers;
    }

    public void addBypassPlayer(GameProfile profile) {
        bypassPlayers.add(profile);
        setDirty();
    }

    public void removeBypassPlayer(GameProfile profile) {
        bypassPlayers.remove(profile);
        setDirty();
    }

    public GameType removePreviousGameMode(UUID playerUUID) {
        var result = previousGameModes.remove(playerUUID);
        if (result != null) {
            setDirty();
        }

        return result;
    }

    public void setPreviousGameMode(UUID playerUUID, GameType gameMode) {
        previousGameModes.put(playerUUID, gameMode);
        setDirty();
    }

    private ForcedGameModeData readNBT(CompoundTag compoundTag) {
        this.bypassPlayers.clear();
        this.previousGameModes.clear();

        if (compoundTag.contains("bypassPlayers")) {
            var list = compoundTag.getList("bypassPlayers", CompoundTag.TAG_COMPOUND);
            for (var tag : list) {
                var innerTag = (CompoundTag) tag;
                var profile = new GameProfile(
                        UUID.fromString(innerTag.getString("id")),
                        innerTag.getString("name")
                );
                bypassPlayers.add(profile);
            }
        }

        if (compoundTag.contains("previousGameModes")) {
            var list = compoundTag.getList("previousGameModes", CompoundTag.TAG_COMPOUND);
            for (var tag : list) {
                var innerTag = (CompoundTag) tag;
                var playerUUID = UUID.fromString(innerTag.getString("playerUUID"));
                previousGameModes.put(playerUUID, GameType.byName(innerTag.getString("gameMode")));
            }
        }

        return this;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var bypassList = new ListTag();
        for (var profile : bypassPlayers) {
            var tag = new CompoundTag();
            tag.putString("id", profile.getId().toString());
            tag.putString("name", profile.getName());
            bypassList.add(tag);
        }
        compoundTag.put("bypassPlayers", bypassList);

        var previousGameModesList = new ListTag();
        for (var entry : previousGameModes.entrySet()) {
            var tag = new CompoundTag();
            tag.putString("playerUUID", entry.getKey().toString());
            tag.putString("gameMode", entry.getValue().getName());
            previousGameModesList.add(tag);
        }
        compoundTag.put("previousGameModes", previousGameModesList);

        return compoundTag;
    }
}
