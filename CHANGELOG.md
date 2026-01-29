# Changelog

## [21.1.17]

### Added

- Added mixin to fix a bug with reliquified twilight forest firefly queen crashing players

## [21.1.16]

### Added

- Force game modes per dimension. When the player moves to a dimension with a forced gamemode, their previous gamemode will be saved and their new gamemode will be applied. When the player leaves the dimension, their previous gamemode will be restored. If no previous gamemode is found, it will default to survival.
  - Configurable via `dimension_forced_gamemodes` in the server config file.
    - This is a mapping of dimension resource locations to gamemodes. This is the `GameType` enum.
  - Commands to bypass forced gamemodes have been added `/ftbpc forcedgamemodes bypass <list|add|remove> <player>` to manage the bypass list.
  - A command to force fix a player has been added `/ftbpc forcedgamemodes fixme`

## [21.1.15]

### Added

* FTB Team stages support for the action pad via the `team_unlocked_at` property in the actions data.

### Fixed

* Fixed an off by one calculation issue causing scrolling on a small list of actions in the action pad gui.

## [21.1.14]

### Added

* Mixin into JER to properly support world-gen ore distributions in modded biomes 

## [21.1.12]

### Added

* Forced Game Rules mapping via `forced-game-rules` in the `ftbpc-common.snbt` config file.
  * This contains a mapping of `rules` that can accept the game rule name as the key and the value as the value to set it to.
  * This can only be set to either a boolean or an integer depending on the game rule type.
  * Incorrect game rule names or incorrect values will throw the game.

## [21.1.11]

### Added

* FTB Chunks integration to allow for custom Y level's based on the CustomYLevel registry added by FTB Chunks

## [21.1.10]

### Changed
* `Teleporter item` has been changed to `Action Pad` to better reflect it's more generic nature

### Fixed
* Shader notice should work again

## [21.1.9]

### Added
* Teleporting item
* Added mixin to force rotation on specific template pools for jigsaw structures (instead of default random rotation)
  * Control via `structure_rotation_override` in `config/ftbpc-server.snbt`
  * This setting is a map of template_pool_id -> rotation ("none", "clockwise_90", "180" or "counterclockwise_90")
* Added trigger block that will call an event when the player steps in the block. You can set a unique ID for this block via the blocks nbt data. This can be used to trigger custom events via other mods or datapacks.
  * Used via NativeEvents in kube `NativeEvents.onEvent('dev.ftb.packcompanion.features.triggerblock.TriggerBlockEvent', event => {`
  * Player, blockpos and uniqueId are available in the event object
* Config to disable wandering trader spawning in with invisible potion effects
* Trigger block, a block that can be placed in world that is invisible but can trigger events when a player walks over it.
* Added config to disable social interactions toast

### Fixed
* Fixed shader onboarding causing crash on dedicated server
* Fixed mod config setup (was being registered too late)

## [21.1.6]

### Changed
* Shader onboarding is now per instance, not per world.

## [21.1.5]

### Added
* Added bias removal option for concentric ring worldgen placement type
  * Controlled by `remove_concentric_ring_placement_bias` config setting (false by default)
* Configurable `Shader Notice` screen that will be presented to players on login. This notice will allow users to pick from shaders being enabled or shaders being disabled. This is useful for modpacks that want to enforce shaders being on or off.
  * Off by default via the common config

## [21.1.4]

### Added
* Added `fancy menu` integrations for checking if the users time is above or below a configurable value

### Changed
* Refactored the codebase and cleaned up the code 

## [21.1.3]

### Added

- Performance improvements around the `/reload` command
  - These are both configurable via the `performance.skip_block_cache_rebuild` config on both the client and server

## [21.1.2]

- Added Chinese translations
- Added Brazilian translations

## [21.1.1]

### Removed

- Pause menu mixins and API. This has moved to FTB Pause Menu API which can be found on CurseForge

## [21.1.0-beta.1]

### Added

- Initial port to 1.21.1
- A relatively simple re-implementation of (https://github.com/MCTeamPotato/SparseStructuresReforged/) for 1.21.1 to bridge the gap until they port. This is allowed per their MIT license.

## [20.4.2]

### Fixed

- Neoforge version having issues with sleeping in beds due to a bad mixin config
- Fabric version not wanting to load due to version restrictions

## [20.4.1]

### Fixed

- MineTogether compatibility

## [20.4.0]

### Changed

- Ported to 1.20.4

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
