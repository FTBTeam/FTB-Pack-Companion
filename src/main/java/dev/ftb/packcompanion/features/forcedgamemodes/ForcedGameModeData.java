package dev.ftb.packcompanion.features.forcedgamemodes;

import com.mojang.authlib.GameProfile;
import dev.ftb.packcompanion.features.schematic.SchematicPasteManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
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
        return SchematicPasteManager.getOverworld(server).getDataStorage().computeIfAbsent(ForcedGameModeData::load, ForcedGameModeData::new, DATA_NAME);
    }

    private static ForcedGameModeData load(CompoundTag compoundTag) {
        return new ForcedGameModeData().readNBT(compoundTag);
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
    public CompoundTag save(CompoundTag compoundTag) {
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
