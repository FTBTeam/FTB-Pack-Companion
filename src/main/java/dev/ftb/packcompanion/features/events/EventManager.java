package dev.ftb.packcompanion.features.events;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.core.utils.MinMax;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum EventManager {
    INSTANCE;

    private final Map<ResourceLocation, EventCategory> categories = new HashMap<>();
    private final Map<EventCategory, List<Event>> events = new HashMap<>();

    private final int cooldown = 20 * 20; // 1 minute in ticks
    private int ticksSinceLastEvent = cooldown;

    EventManager() {
        registerCategory(PackCompanion.id("general"), "packcompanion.event.category.general", "packcompanion.event.category.general.desc", Icon.getIcon("minecraft:item/stone"));
    }

    public void registerCategory(ResourceLocation id, String name, String description, @Nullable Icon icon) {
        if (categories.containsKey(id)) {
            throw new IllegalArgumentException("Event category with id " + id + " is already registered");
        }

        var category = new EventCategory(id, name, description, icon);
        categories.put(id, category);
    }

    public void registerEvent(ResourceLocation category, Event event) {
        var cat = categories.get(category);
        if (cat == null) {
            throw new IllegalArgumentException("Event category with id " + category + " is not registered");
        }

        events.computeIfAbsent(cat, c -> new ArrayList<>()).add(event);
    }

    public void runEventLoop(MinecraftServer server) {
        // No events = no-op
        var availableEvents = events.values().stream().flatMap(List::stream).toList();
        if (availableEvents.isEmpty()) {
            return;
        }

        if (++ticksSinceLastEvent < cooldown) {
            return;
        }
        ticksSinceLastEvent = 0;

        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Pick a random player to attempt to run an event for.
        var player = players.get(new Random().nextInt(players.size()));
        if (player == null || !player.isAlive() || !player.gameMode.isSurvival()) {
            return;
        }

        var eventShuffle = new ArrayList<>(availableEvents);
        Collections.shuffle(eventShuffle);

        for (Event event : eventShuffle) {
            if (Math.random() <= event.chance()) {
                var location = locateSpawnPosition(player.level(), player, event);
                var context = new EventContext((ServerLevel) player.level(), player, location);
                event.action(context);
                break; // Only one event per loop.
            }
        }
    }

    @Nullable
    private BlockPos locateSpawnPosition(Level level, Player player, Event event) {
        EventConfig.LocationSettings locationSettings = event.config().locationSettings();

        // If no location settings, we can't generate a position.
        if (locationSettings == null) {
            return null;
        }

        var playerPos = player.blockPosition();

        int attempts = 0;
        while (++attempts < 10) {
            // Pick a random block around the player within the action area.
            MinMax actionArea = locationSettings.actionAreaFromPlayer();
            int dx = (int) (Math.random() * (actionArea.max() - actionArea.min()) + actionArea.min());
            int dz = (int) (Math.random() * (actionArea.max() - actionArea.min()) + actionArea.min());

            // Allow a deviation of +/- 10 blocks on the y-axis.
            int dy = (int) (Math.random() * 20 - 10);
            BlockPos pos = playerPos.offset(dx, dy, dz);

            // Now we check the rules for this location to match
            var location = getLocationIfRulesMatch(level, pos, locationSettings);
            if (location != null) {
                return pos;
            }
        }

        return null;
    }

    @Nullable
    private BlockPos getLocationIfRulesMatch(Level level, BlockPos pos, EventConfig.LocationSettings locationSettings) {
        var area = locationSettings.actionAreaFromPlayer();

        var boundingBox = new BoundingBox(pos).inflatedBy(locationSettings.requiredSpaceNeededForActionArea() / 2);
        var blocksWithinArea = BlockPos.betweenClosedStream(boundingBox)
                .filter(boundingPos -> area.min() == 1 || boundingPos.distSqr(pos) >= area.min() * area.min())
                .toList();

        // Get the lowest y value in the area.
        int minY = blocksWithinArea.stream().mapToInt(BlockPos::getY).min().orElse(pos.getY());

        // Check for solid footing if required.
        if (locationSettings.requiresSolidFooting()) {
            boolean hasFooting = false;
            for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
                for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                    // The location under each of the x,z positions at the minimum y level.
                    BlockPos footingPos = new BlockPos(x, minY - 1, z);
                    BlockState state = level.getBlockState(footingPos);
                    if (!state.isAir() && !state.canBeReplaced()) {
                        hasFooting = true;
                        break;
                    }
                }
            }

            if (!hasFooting) {
                return null;
            }
        }

        // Get the center position of the space at the minY level. (floor)
        BlockPos centerPos = new BlockPos(pos.getX(), minY, pos.getZ());

        // Check for required blocks if any are specified.
        var requiredBlocks = locationSettings.requiredBlocksInSpawnArea();
        if (requiredBlocks.isEmpty()) {
            return centerPos;
        }

        // TODO: Tag support?
        boolean foundAtLeastOne = false;
        for (var block : blocksWithinArea) {
            BlockState state = level.getBlockState(block);
            if (requiredBlocks.stream().anyMatch(b -> b.is(state.getBlock()))) {
                foundAtLeastOne = true;
                break;
            }
        }

        return foundAtLeastOne ? centerPos : null;
    }
}
