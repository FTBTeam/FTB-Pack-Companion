# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [20.1.2]

### Added

- Backported a bunch of features from 1.21
  - Action pad, a simple item that allows a user to run a predefined list of actions, like teleport locations, tpa, etc
  - Forced GameRules, force certain gamerules to be set on a server or singleplayer world upon world load
  - Shader notice, if shaders are on the pack, you can enable a notification to let users know and to opt-in to having shaders enabled
  - Trigger block, an invisible block that will trigger a Forge event when a player walks over it with a small delay to prevent spam
  - Ability to disable wandering traders from being able to use their invisibility potion on spawn

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
