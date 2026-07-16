package dev.ftb.packcompanion.features.kube.mods;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.waypoint.Waypoint;
import dev.ftb.mods.ftbchunks.api.client.waypoint.WaypointManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTBChunksClient {
    public static final FTBChunksClient INSTANCE = new FTBChunksClient();

    // Provide a quick lookup solution for resource location -> resource key
    private final Map<ResourceLocation, ResourceKey<Level>> dimensionIdToResourceKeyMap = new HashMap<>();

    public WaypointManager waypointManagerForCurrentDim() {
        return FTBChunksAPI.clientApi().getWaypointManager().orElse(null);
    }

    public WaypointManager waypointManagerForLevel(Level level) {
        return waypointManagerForKey(level.dimension());
    }

    public WaypointManager waypointManagerForKey(ResourceKey<Level> dimensionKey) {
        return FTBChunksAPI.clientApi().getWaypointManager(dimensionKey).orElse(null);
    }

    public List<Waypoint> allWaypointsForCurrentDim() {
        return allWaypoints(waypointManagerForCurrentDim());
    }

    public List<Waypoint> allWaypointsForLevel(Level level) {
        return allWaypoints(waypointManagerForLevel(level));
    }

    private List<Waypoint> allWaypoints(WaypointManager manager) {
        return manager != null ? manager.getAllWaypoints().stream().toList() : List.of();
    }

    public WaypointManager waypointManagerForDimId(ResourceLocation dimensionId) {
        var resourceKey = dimensionIdToResourceKeyMap.computeIfAbsent(dimensionId, e ->
                ResourceKey.create(Registries.DIMENSION, dimensionId));

        return waypointManagerForKey(resourceKey);
    }

    public void requestMinimapIconRefresh() {
        FTBChunksAPI.clientApi().requestMinimapIconRefresh();
    }

    public Waypoint addWaypoint(BlockPos pos, String name) {
        return createWaypoint(waypointManagerForCurrentDim(), pos, name);
    }

    public Waypoint addWaypointIn(Level level, BlockPos pos, String name) {
        return createWaypoint(waypointManagerForLevel(level), pos, name);
    }

    private Waypoint createWaypoint(WaypointManager waypointManager, BlockPos pos, String name) {
        if (waypointManager == null) {
            return null;
        }

        return waypointManager.addWaypointAt(pos, name);
    }

    public boolean removeWaypoint(Waypoint waypoint) {
        return removeWaypoint(waypointManagerForCurrentDim(), waypoint);
    }

    public boolean removeWaypointIn(Level level, Waypoint waypoint) {
        return removeWaypoint(waypointManagerForLevel(level), waypoint);
    }

    private boolean removeWaypoint(WaypointManager waypointManager, Waypoint waypoint) {
        if (waypointManager == null || waypoint == null) {
            return false;
        }

        return waypointManager.removeWaypoint(waypoint);
    }
}
