package dev.ftb.packcompanion.features.forcedgamemodes;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ForcedGameModeData extends SavedData {
    static final Logger LOGGER = LoggerFactory.getLogger(ForcedGameModeData.class);

    public static final Codec<ForcedGameModeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NameAndId.CODEC.listOf().fieldOf("bypassPlayers")
                    .forGetter(data -> new ArrayList<>(data.bypassPlayers)),
            Codec.unboundedMap(
                            UUIDUtil.CODEC,
                            GameType.CODEC
                    ).fieldOf("previousGameModes")
                    .forGetter(data -> data.previousGameModes)
    ).apply(instance, (bypassList, previousGameModes) ->
            new ForcedGameModeData(new HashSet<>(bypassList), previousGameModes)));

    public static final SavedDataType<ForcedGameModeData> TYPE = new SavedDataType<>(
            PackCompanion.id("forced_gamemodes"),
            ForcedGameModeData::new,
            CODEC
    );

    private final Set<NameAndId> bypassPlayers;
    private final Map<UUID, GameType> previousGameModes;

    public ForcedGameModeData(Set<NameAndId> bypassPlayers, Map<UUID, GameType> previousGameModes) {
        this.bypassPlayers = bypassPlayers;
        this.previousGameModes = previousGameModes;
    }

    public ForcedGameModeData() {
        this.bypassPlayers = new HashSet<>();
        this.previousGameModes = new HashMap<>();
    }

    public static ForcedGameModeData getInstance(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

//    private static ForcedGameModeData load(CompoundTag tag, HolderLookup.Provider provider) {
//        return new ForcedGameModeData().readNBT(tag);
//    }

    public Set<NameAndId> bypassPlayers() {
        return bypassPlayers;
    }

    public void addBypassPlayer(GameProfile profile) {
        bypassPlayers.add(new NameAndId(profile));
        setDirty();
    }

    public void removeBypassPlayer(GameProfile profile) {
        bypassPlayers.remove(new NameAndId(profile));
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
}
