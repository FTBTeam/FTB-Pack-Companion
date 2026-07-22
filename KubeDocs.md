# Pack companion Kube Addons

Pack companion adds some utilities on to the KubeJS API. This document is a reference for the utilities provided as a 'quick' lookup source, If you'd like more documentation on what each method does. Please refer to the [comments on the sourcecode](./src/main/java/dev/ftb/packcompanion/features/kube/)

## Core addons

### `_` General Utilities

- [`id` Identifier helpers](#_id-identifier-helpers)
- [`utils` General utility functions]()
- [`str` String manipulation utilities](#_str-string-utilities)
- `getOrDefault(T value, T defaultValue)`
- `isUnset(Object value)`
- `isSet(Object value)`

### `_.id` Identifier Helpers

- `create(String namespace, String path)`
- `parseOrNull(String id)`
- `ftb(String path)`
- `mc(String path)`
- `kube(String path)`

### `_.utils` General Utility Functions

- `NIL_UUID`
- `ZERO_BLOCKPOS`
- `ZERO_VECTOR3F`
- `ZERO_VECTOR3D`
- `ZERO_VECTOR3I`
- `ZERO_AABB`
- `randomUUID()`
- `randomUUIDString()`
- `vec3(Number x, Number y, Number z)`
- `vec3d(Number x, Number y, Number z)`
- `vec3f(Number x, Number y, Number z)`
- `blockPos(Number x, Number y, Number z)`
- `boundingFromCorners(BlockPos min, BlockPos max)`
- `boundingSized(BlockPos pos, Number size)`
- `isFakePlayer(Player player)`
- `isRealPlayer(Player player)`
- `isOp(Player player)`

### `_.str` String Utilities

- `capitalize(String input)`
- `titleCase(String input)`
- `isEmpty(String input)`
- `isBlank(String input)`
- `truncate(String input, int maxLength)`
- `padLeft(String input, int length, char padChar)`
- `padRight(String input, int length, char padChar)`

## Mod addons

When specific mods are present, Pack Companion will register additional utilities to help with those mods. 

### `ftbquests` FTB Quests Utilities

Please note: Any reference to `String questId` is referring to the quest's `long` identifier encoded as a `hex` string. This string is provided to you when you right-click on a quest and `copy id`

- `isCompleted(Player player, String questId)`
- `isStarted(Player player, String questId)`
- `relativeQuestProgress(Player player, String questId)`
- `isLocked(Player player, String questId)`
- `pinQuest(Player player, String questId)`
- `unpinQuest(Player player, String questId)`
- `isPinned(Player player, String questId)`
- `hasUnclaimedRewards(Player player, String questId)`
- `getTeamData(Player player)`
- `getObjectById(String id)`

### `ftbteams` FTB Teams Utilities

- `manager()`
- `clientManager()`
- `teamIdFromPlayer(ServerPlayer player)`
- `teamIdFromPlayerId(UUID playerId)`
- `teamForPlayer(ServerPlayer player)`
- `shortTeamName(Team team)`
- `shortTeamNameFromPlayer(ServerPlayer player)`
- `teamById(UUID teamId)`

### `ftbchunks` FTB Chunks Utilities

**Client** (`ftbchunksclient`)

- `waypointManagerForCurrentDim()`
- `waypointManagerForLevel(Level level)`
- `waypointManagerForKey(ResourceKey<Level> dimensionKey)`
- `allWaypointsForCurrentDim()`
- `allWaypointsForLevel(Level level)`
- `allWaypoints(WaypointManager manager)`
- `waypointManagerForDimId(ResourceLocation dimensionId)`
- `requestMinimapIconRefresh()`
- `addWaypoint(BlockPos pos, String name)`
- `addWaypointIn(Level level, BlockPos pos, String name)`
- `createWaypoint(WaypointManager waypointManager, BlockPos pos, String name)`
- `removeWaypoint(Waypoint waypoint)`
- `removeWaypointIn(Level level, Waypoint waypoint)`
- `removeWaypoint(WaypointManager waypointManager, Waypoint waypoint)`

**Common / Server** (`ftbchunks`)

- `chunkData(Level level, int x, int z)`
- `isClaimed(Level level, int x, int z)`
- `isForceLoaded(Level level, int x, int z)`
- `teamIdForChunk(Level level, int x, int z)`
- `chunkOwner(Level level, int x, int z)`
- `claimChunk(ServerPlayer player, Level level, int x, int z)`
