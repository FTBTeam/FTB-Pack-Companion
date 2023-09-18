# Changelog

## [2.0.7]

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
