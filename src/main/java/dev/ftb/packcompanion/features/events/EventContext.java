package dev.ftb.packcompanion.features.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Context provided to an event when it is executed.
 *
 * @param level The level the event is being executed in.
 * @param player The player the event is being executed for.
 * @param spawnPos A location generally near the player where the event is taking place.
 *                 This can be null if the event doesn't require a position to be generated
 */
public record EventContext(
        ServerLevel level,
        Player player,
        @Nullable BlockPos spawnPos
) {}
