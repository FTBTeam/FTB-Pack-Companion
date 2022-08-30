package dev.ftb.packcompanion.features.loot;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.registry.LootTableRegistries;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RandomNameLootFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomNameLootFunction.class);
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    // Stolen from minecraft!
    private static final Gson GSON = Util.make(() -> {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
        gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
        gsonBuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        return gsonBuilder.create();
    });

    private static Map<String, List<Component>> namingData = null;
    private static boolean hasAttemptedLoad = false;

    final String nameSetKey;

    RandomNameLootFunction(LootItemCondition[] lootItemConditions, @Nullable String arg) {
        super(lootItemConditions);
        this.nameSetKey = arg;
    }

    public static void clearCache() {
        LOGGER.debug("Clearing RandomNameLootFunction source data");
        namingData = null;
        hasAttemptedLoad = false;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.nameSetKey == null) {
            return itemStack;
        }

        // Load the file from the mods resources
        Optional<Resource> namingSource = lootContext.getLevel().getServer().getResourceManager().getResource(new ResourceLocation(PackCompanion.MOD_ID, "sources/random-name-loot-source.json"));

        if (namingSource.isEmpty()) {
            LOGGER.warn("Attempted RandomNameLootFunction with no random-name-loot-source.json file in the data/ftbpc/sources folder");
            return itemStack;
        }

        if (hasAttemptedLoad && namingData == null) {
            LOGGER.warn("Attempted to load naming data for RandomNameLootFunction but no data was found");
            return itemStack;
        }

        if (namingData == null) {
            hasAttemptedLoad = true;

            // Load the data... finally...
            try {
                namingData = GSON.fromJson(namingSource.get().openAsReader(), new TypeToken<Map<String, List<Component>>>() {}.getType());
            } catch (Exception e) {
                LOGGER.error("Error trying to read", e);
            }
        }

        if (!namingData.containsKey(this.nameSetKey)) {
            LOGGER.warn("Attempted to use RandomNameLootFunction without supplying a valid name for the file lookup");
            return itemStack;
        }

        List<Component> names = namingData.get(this.nameSetKey);
        Component nameToUse = names.get(RANDOM_SOURCE.nextInt(names.size()));

        itemStack.setHoverName(nameToUse);
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootTableRegistries.RANDOM_NAME_LOOT_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomNameLootFunction> {
        @Override
        public void serialize(JsonObject jsonObject, RandomNameLootFunction arg, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, arg, jsonSerializationContext);
            if (arg.nameSetKey != null) {
                jsonObject.addProperty("nameSetKey", arg.nameSetKey);
            }
        }

        @Override
        public RandomNameLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] args) {
            return new RandomNameLootFunction(args, jsonObject.get("nameSetKey").getAsString());
        }
    }
}
