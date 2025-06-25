package dev.ftb.packcompanion.features.loot;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.packcompanion.PackCompanion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RandomNameLootFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomNameLootFunction.class);
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private static Map<String, List<Component>> namingData = null;
    private static boolean hasAttemptedLoad = false;

    final String nameSetKey;

    public static final MapCodec<RandomNameLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .and(Codec.STRING.optionalFieldOf("nameSetKey").forGetter(x -> Optional.ofNullable(x.nameSetKey)))
                    .apply(instance, RandomNameLootFunction::new)
    );

    public RandomNameLootFunction(List<LootItemCondition> lootItemConditions, Optional<String> arg) {
        super(lootItemConditions);
        this.nameSetKey = arg.orElse("");
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
        Optional<Resource> namingSource = lootContext.getLevel().getServer().getResourceManager().getResource(PackCompanion.id("sources/random-name-loot-source.json"));

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
                namingData = createGson(lootContext.getLevel().registryAccess())
                        .fromJson(namingSource.get().openAsReader(), new TypeToken<Map<String, List<Component>>>() {}.getType());
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

        itemStack.set(DataComponents.CUSTOM_NAME, nameToUse);
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return RandomNameLootFeature.RANDOM_NAMED_LOOT_FUNCTION.get();
    }

    private static Gson createGson(RegistryAccess registryAccess) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter(registryAccess));
        gsonBuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        return gsonBuilder.create();
    }
}
