package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerBlockController {
    private static final TriggerBlockController CLIENT_INSTANCE = new TriggerBlockController();
    private static final TriggerBlockController SERVER_INSTANCE = new TriggerBlockController();

    private final Map<TriggerId, Instant> playersInTriggers = new HashMap<>();
    private short tickTracker = 0; // Overflowing is intential. We only care about the first X numbers

    public static TriggerBlockController getInstance(boolean client) {
        return client ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }

    void onTick() {
        // Once a second, clean up old entries.
        if (++tickTracker % 20 != 0) {
            return;
        }

        var mapCopy = new HashMap<>(playersInTriggers);
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
        for (var entry : mapCopy.entrySet()) {
            // If the entry is older than 5 seconds, remove it.
            if (entry.getValue().isBefore(fiveSecondsAgo)) {
                playersInTriggers.remove(entry.getKey());
            }
        }
    }

    public void onPlayerIn(Player player, BlockPos pos) {
        var entity = player.level().getBlockEntity(pos);
        if (!(entity instanceof TriggerBlockEntity triggerBlockEntity)) {
            // Not a trigger block entity?
            return;
        }

        String name = triggerBlockEntity.name();

        var key = new TriggerId(player, name);
        if (playersInTriggers.containsKey(key)) {
            // Already recorded.
            return;
        }

        playersInTriggers.put(key, Instant.now());
        if (triggerBlockEntity.ignorePlayersWithTag() != null) {
            if (player.getTags().contains(triggerBlockEntity.ignorePlayersWithTag())) {
                return;
            }
        }

        // Trigger an event.
        MinecraftForge.EVENT_BUS.post(new TriggerBlockEvent(player, pos, name));
    }

    record TriggerId(UUID playerId, String id) {
        TriggerId(Player player, String id) {
            this(player.getUUID(), id);
        }
    }
}
