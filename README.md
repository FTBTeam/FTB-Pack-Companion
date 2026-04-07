# FTB Pack Companion

Your best friend when developing a Modpack. With an ever-growing list of features, tools, game fixes and more that'll aid in the creation, maintenance and stability of your pack. There is nothing better than bring along a good companion.

## Features for `26.1+`

* [Action Pad](#action-pad)
  * Teleportation item / and general purpose action item that can be used to display entries to the player when used.
* [Forced GameRules](#forced-gamerules)
  * Mapping of forced game-rules that will be applied on world creation / load.
* [Shader Notice](#shader-notice)
  * Presents the player with a shader notice screen on the first world load of the instance to allow them to pick shaders on or off.
* [Spawner Behaviour Modifications](#spawner-behaviour-modifications)
  * Custom spawner behaviour including:
  * Punish the player on break
  * Prevent breaking of spawners
  * Break torches / light sources near a spawner
  * Modify the difficulty of the spawners mobs via a custom attribute
* [Structure Rotation Overrides](#structure-rotation-overrides)
  * Force a specific rotation on jigsaw structures via config instead of random rotation.
* [WaterLogging Structure Processor Fixer](#waterlogging-structure-processor-fixer)
  * Fixes issues with waterlogged blocks in structures via a custom structure processor.
* [Trigger Block](#trigger-block)
  * An invisible block that can trigger events when a player walks over it.
* [No wandering trader invisible](#no-wandering-trader-invisible)
  * Config to disable wandering trader spawning in with invisible potion effects.
* [Toast Removals](#toast-removals)
  * Removal of tutorial toasts, social interaction toasts and more via config.
* [Forced Seed (Single player)](#forced-seed)

### Action Pad

Multipurpose action item that will display a user friendly GUI when used. This can be used for teleporting or running a command.

`ftbpc_pad_actions.json5`

**Config**
```json5
{
  actions: [
    {
      command_action: {
        command: "tp @s ~5 ~5 ~5",
        execution_level: 4
      },
      name: "lol",
      icon: "ftblibrary:icons/globe"
    },
    {
      teleport_action: {
        position: [
          100,
          64,
          -100
        ],
        dimension: "minecraft:the_nether",
        rotation: [
          0.0,
          0.0
        ]
      },
      name: "Nether Teleport",
      icon: "ftblibrary:icons/nether_portal"
    }
  ]
}
```

### Forced GameRules

The Forced GameRules feature allows you to set specific game rules that will be applied whenever a world is created or loaded. This is controlled via the `forced-game-rules` setting in the `ftbpc-common.snbt` config file.

```json5
{
  misc: {
    forced_game_rules: {
      randomTickSpeed: 3,
      doDaylightCycle: false
    }
  }
}
```

### Shader Notice

A simple shader notice screen that will let the user know that shaders can be used and warn them that performance may be impacted. This screen will only show once per instance.

`ftbpc-common.json5`
```json5
{
	shaders_notice: {
		shader_pack_to_use: "", // Leave blank to pick the first available shader pack
		show_on_start: false
	}
}
```

### Spawner Behaviour Modifications

Spawners default behaviour can be modified via the `ftbpc-server.snbt` config file.

Spawner behaviour options include:
* Forcing spawners to not be breakable
* Punishing players for breaking spawners
* Random entity spawning from spawners
* Respawning of spawners after broken
* Respawn interval modification

```json5
{
  spawners: {
    allow_respawn: false,
    punish_for_breaking_spawners: false,
    random_entity: [],
    respawn_interval: 60
  }
}
```

### Structure Rotation Overrides

Sometimes you may want a specific structure to have a set rotation when spawned in. This can be done with any structure that has the type of `minecraft:jigsaw`

This accepts a map of template pool ID's to a forced rotation for that pool: one of 'none', 'clockwise_90', '180', 'counterclockwise_90'

`ftbpc-server.json5`
```json5
{
    worldgen: {
        structure_rotation_override: {
            "minecraft:village/plains/houses": "clockwise_90"
            "minecraft:stronghold/corridors": "180"
        }
    }
}
```

### Trigger Block

The Trigger Block is an invisible block that can be placed in the world and will trigger events when a player walks over it. You can set a unique ID for this block via the block's NBT data. This can be used to trigger custom events via KubeJS

You can listen for the event in KubeJS like this:

```javascript
NativeEvents.onEvent('dev.ftb.packcompanion.features.triggerblock.TriggerBlockEvent', event => {
    const player = event.player;
    const blockPos = event.pos;
    const uniqueId = event.identifier;
    
    // Do something.
});
```

This block has a delay to triggering the next event of 5 seconds to prevent spam triggering.

### No wandering trader invisible

Sometimes, specifically in no-sky dimensions, it would be preferred to disable the wandering trader from being able to trigger their invisible potion effect on spawn. This can be disabled via the `ftbpc-server.snbt` config file.

```json5
{
    misc: {
        no_wandering_trader_invis_potions: true
    }
}
```

### Toast Removals

You can disable specific toasts via the `ftbpc-client.json5` config file.

```json5
{
  toasts: {
    disable_advancements_toasts: true,
    disable_recipe_toasts: true,
    disable_socialinteraction_toasts: true,
    disable_tutorial_toasts: true
  }
}
```

### Forced Seed

You can force a new world to have a specific seed in singleplayer via the `ftbpc-client.snbt` config file.

```json5
{
  worldgen: {
    static_seed: 1234567890,
  }
}
```

### WaterLogging Structure Processor fixer

The WaterLogging fix is applied directly into your `template_pool`. When creating a `pool` add the processor `ftbpc:waterlogging_fix_processor` to your processors list. The companion will now figure out when a block shouldn't be water logged and fix it!

It should look like this

```json
{
  "processors": [
    {
      "processor_type": "ftbpc:waterlogging_fix_processor"
    }
  ]
}
```

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![](https://cdn.feed-the-beast.com/assets/socials/icons/social-discord.webp)](https://go.ftb.team/discord) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-github.webp)](https://go.ftb.team/github) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitter-x.webp)](https://go.ftb.team/twitter) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-youtube.webp)](https://go.ftb.team/youtube) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitch.webp)](https://go.ftb.team/twitch) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-instagram.webp)](https://go.ftb.team/instagram) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-facebook.webp)](https://go.ftb.team/facebook) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-tiktok.webp)](https://go.ftb.team/tiktok)
