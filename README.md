# FTB Pack Companion

Your best friend when developing a Modpack. With an ever growing list of features, tools, game fixes and more that'll aid in the creation, maintenance and stability of your pack. There is nothing better than bring along a good companion.

## Features so far

- Structure Processor to resolve issues with WaterLogging blocks that should not be water logged
- Command to help you generate loot tables in game!
- A new `RandomNameLootFunction` that lets you set a list of names for an item and have the function select one at random

## Using the features

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

### `create_loot_table` command

This one is super helpful, it'll allow you to create a loot table based on in-game items with their current data. 

```java
// TODO, show how this works
```

### RandomNameLootFunction

The `RandomNameLootFunction` it a `LootItemFunction` that allows you to set an item's name based on a list of names. This requires a list of `Component`'s in `JSON` format to be placed in the `data/ftbpc/sources/random-name-loot-source.json` folder. You can use a `datapack` or `KubeJS` to modify this file as by default, it simply contains an example.

**Example of how the json file should look**

```json
{
  "example": [
    {"bold":true,"color":"blue","text":"I'm an examples!"}
  ],
  "list2": [
    {"text":"I'm an example 2!"},
    {"text":"More than just one"}
  ]
}
```

Each key in the Map / Object List must be unique as it's used as the identifier category for the loot function to find a random name. As you can see, you need to use a fully JSON compliant `Component` for the function to work correctly. Errors will be logged if this is done wrong.

**Here is what the loot table can look like to take advantage of the above json file**

```json
{
  "entries": [
    {
      "type": "minecraft:item",
      "name": "minecraft:stone",
      "functions": [
        {
          "function": "ftbpc:random_loot_item_function",
          "nameSetKey": "example"
        }
      ]
    },
    {
      "type": "minecraft:item",
      "name": "minecraft:gold",
      "functions": [
        {
          "function": "ftbpc:random_loot_item_function",
          "nameSetKey": "list2"
        }
      ]
    }
  ]
}
```
