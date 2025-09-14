package dev.ftb.packcompanion.features.events;

import dev.ftb.packcompanion.core.utils.MinMax;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Configuration options for an event.
 *
 */
@SuppressWarnings("unused")
public record EventConfig(
        @Nullable LocationSettings locationSettings,
        @Nullable String unlockedStage
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean requiresLevelLocation = false;

        private boolean requiresSolidFooting = false;
        private String unlockedStage = null;

        private int requiredSpaceNeededForActionArea = 3; // 3x3 area
        private MinMax actionAreaFromPlayer = MinMax.exact(5); // 5 blocks away from player
        private List<BlockState> requiredBlocksInSpawnArea = List.of();

        private Builder() {
        }

        public Builder setRequiresSolidFooting(boolean requiresSolidFooting) {
            this.requiresSolidFooting = requiresSolidFooting;
            return this;
        }

        public Builder setUnlockedStage(@Nullable String unlockedStage) {
            this.unlockedStage = unlockedStage;
            return this;
        }

        public Builder setRequiredSpaceNeededForActionArea(int requiredSpaceNeededForActionArea) {
            this.requiresLevelLocation = true;
            this.requiredSpaceNeededForActionArea = requiredSpaceNeededForActionArea;
            return this;
        }

        public Builder setActionAreaFromPlayer(MinMax actionAreaFromPlayer) {
            this.requiresLevelLocation = true;
            this.actionAreaFromPlayer = actionAreaFromPlayer;
            return this;
        }

        public Builder setRequiredBlocksInSpawnArea(List<BlockState> requiredBlocksInSpawnArea) {
            this.requiresLevelLocation = true;
            this.requiredBlocksInSpawnArea = requiredBlocksInSpawnArea;
            return this;
        }

        public EventConfig build() {
            LocationSettings locationSettings = null;
            if (requiresLevelLocation) {
                locationSettings = new LocationSettings(
                        requiredBlocksInSpawnArea,
                        actionAreaFromPlayer,
                        requiredSpaceNeededForActionArea,
                        requiresSolidFooting
                );
            }

            return new EventConfig(
                    locationSettings,
                    unlockedStage
            );
        }
    }

    public record LocationSettings(
        List<BlockState> requiredBlocksInSpawnArea,
        MinMax actionAreaFromPlayer,
        int requiredSpaceNeededForActionArea,
        boolean requiresSolidFooting // Requires a solid block under the location
    ) {}
}
