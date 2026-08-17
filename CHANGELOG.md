# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [20.1.12]

### Fixed
* `AsyncChunkLoader` now requests chunks with a chunk ticket instead of force-loading them up front, so chunks are only marked as forced once they have actually loaded
* Chunk tickets are released when a job finishes, fails, or times out
* A warning is now logged when a chunk load job gives up after the timeout

## [20.1.11]

### Fixed
* More fixes around world blocking generation with async fixes

## [20.1.10]

### Fixed
* Async issue with thread blocking

## [20.1.9]

### Added
* Support for FTB Essentials integration allowing the Action pad to have it's TPA option disabled if Essentials has TPA disabled 

### Removed
* Removed the `misc.enable_tpa_action_pad` config option to the server config as it is now handled by FTB Essentials in preference for Essentials integration

## [20.1.8]

### Added
* Added `misc.enable_tpa_action_pad` config option to the server config to allow disabling the TPA action pad feature

## [20.1.7]

### Added
* Added `AsyncChunkLoader` utility class

## [20.1.6]

### Added
* Improved to schema system
  * Pastes now seal the schematic area in a temporary shell (seal_perimeter, shell_block) to stop fluids/falling blocks leaking in mid-paste.
  * After pasting, leaked fluids are cleaned up automatically (cleanup_fluids); falling blocks can optionally be cleaned too (cleanup_falling_blocks).
  * Shell is removed after paste by default, or can be kept permanently (remove_shell_after_paste).
  * Cleanup scan speed is tunable via cleanup_scan_multiplier.

## [20.1.5]

### Added

- Support for biome setting via the schmatic schema

### Fixed

- Schematic pasting issues with resource cleanup, block replaing, and how we handle placing over ticks

## [20.1.4]

### Added
- Trigger block enhancements
  - Trigger block event is now reliably fired on both client and server sides
  - Trigger blockentity data is now sync'd to client:
    - Creative mod pick block works: easy placement of multiple triggers with same ID
    - The triger ID is now also available to clientside event handlers (`event.getIdentifier()`)

### Fixed
- Fixed reobfuscation issues in published JAR
- Curios support not working for the Action pad

## [20.1.3]

### Added

- Force game modes per dimension. When the player moves to a dimension with a forced gamemode, their previous gamemode will be saved and their new gamemode will be applied. When the player leaves the dimension, their previous gamemode will be restored. If no previous gamemode is found, it will default to survival.
  - Configurable via `dimension_forced_gamemodes` in the server config file.
    - This is a mapping of dimension resource locations to gamemodes. This is the `GameType` enum.
  - Commands to bypass forced gamemodes have been added `/ftbpc forcedgamemodes bypass <list|add|remove> <player>` to manage the bypass list.
  - A command to force fix a player has been added `/ftbpc forcedgamemodes fixme`

## [20.1.2]

### Added

- Backported Minecrafts command history feature
- Backported a bunch of features from 1.21
  - Action pad, a simple item that allows a user to run a predefined list of actions, like teleport locations, tpa, etc
  - Forced GameRules, force certain gamerules to be set on a server or singleplayer world upon world load
  - Shader notice, if shaders are on the pack, you can enable a notification to let users know and to opt-in to having shaders enabled
  - Trigger block, an invisible block that will trigger a Forge event when a player walks over it with a small delay to prevent spam
  - Ability to disable wandering traders from being able to use their invisibility potion on spawn
  - 
### Fixed

- Minor issue with the action pad's GUI not correctly calculating its height.

## [2.3.0]

### Added

- Grid Placement for structures, useful for repeating or statically placed structures in a world.

## [2.2.1]

### Added

- JEI Recipe category sorting via config

## [2.2.0]

### Added

- Support for modmenu on the custom pause screen (on fabric)

### Fixed

- Create's options button placement
- Spawner punishment mobs not spawning with the correct nbt data
- Api contract now supports a list of providers per target


## [2.0.7]

### Added

- Added configurable "punishment" when a Mob spawner is broken. This will spawn more of the entity that is contained within the spawner around the player. This can be anything from 2 - 8 mobs spawned around the spawner using a flood fill algorithm. This is only active when spawner respawn is running.

### Fixed

- Fixed an issue causing entities health to be buffed on each world load instead of just once. This has been fixed by moving to a attribute modifier instead of a flat health increase. The `uuid` for this attribute modifier is `a07a9434-d6c2-44f1-b5eb-394da41c9f9f`.

## [2.0.6]

### Added
- Ability to increase maximum jigsaw size from 128 to 256
  - Controlled via `extended_jigsaw_range` config setting (default true)
- Added `Mob base health modifier` via the settings config `modify_mob_base_health` in the world/serverconfig/ file
- Added `Spawner Respawn` feature
  - When enabled, this allows broken spawners to be remembered and will respawn at a given interval.
  - You can configure the interval in minutes at which spawners will respawn via the `respawn_interval`
  - You can configure the list of entity types that will be used to replace broken spawners via the `random_entity` list

## [2.0.5]

### Fixed

- Fix waterlogging fix processor crash for huge structure which could go outside the generated chunk region during worldgen
- Make the waterlogging fix processor also work with `ServerLevel`, so it can be used with the `/place jigsaw ...` command

## [2.0.4]

### Added

- Support for having a static world seed in single player. Can be configured in the `client` config.

## [2.0.3]

### Added

- Support for disabling Advancement Toasts 
- Support for disabling Recipe Toasts

### Changed 

- Moved over to SNBT config files instead of json. Please be sure to update your config files if you use this mod in a non-ftb modpack.

## [2.0.2]

### Fixed

- Hopefully fixed the water logging issues in structure using our processor

## [2.0.1]

### Added

- The ability to use beds in any dimension

## [2.0.0]

### Added

- Released the mod for 1.19.2
