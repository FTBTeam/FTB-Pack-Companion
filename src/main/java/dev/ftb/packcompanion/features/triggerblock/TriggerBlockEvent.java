package dev.ftb.packcompanion.features.triggerblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class TriggerBlockEvent extends Event {
    private final Player player;
    private final BlockPos pos;
    private final String identifier;

    public TriggerBlockEvent(Player player, BlockPos pos, String identifier) {
        this.player = player;
        this.pos = pos;
        this.identifier = identifier;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "TriggerBlockEvent{" +
                "player=" + player +
                ", pos=" + pos +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
