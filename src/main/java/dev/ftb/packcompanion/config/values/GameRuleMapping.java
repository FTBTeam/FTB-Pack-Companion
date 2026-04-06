package dev.ftb.packcompanion.config.values;

import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.config.value.AbstractMapValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameRuleMapping extends AbstractMapValue<Either<Integer, Boolean>> {
    private static final Codec<Either<Integer, Boolean>> GAME_RULE_VALUE_CODEC = Codec.either(Codec.INT, Codec.BOOL);

    public GameRuleMapping(@Nullable Config parent, String key) {
        super(parent, key, new HashMap<>(), GAME_RULE_VALUE_CODEC);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        super.read(serializer);

        // Validate that all the keys are gamerules and validate they all match their expected types
        var registry = BuiltInRegistries.GAME_RULE;
        for (Map.Entry<String, Either<Integer, Boolean>> mapping : get().entrySet()) {
            GameRule<?> value = registry.getValue(Identifier.tryParse(mapping.getKey()));

            if (value == null) {
                throw new IllegalStateException("Unknown game rule key in config: " + mapping.getKey());
            }

            ArgumentType<?> argument = value.argument();
            if (argument instanceof BoolArgumentType && mapping.getValue().left().isPresent()) {
                throw new IllegalStateException("Expected boolean value for game rule '" + mapping.getKey() + "' but got integer");
            } else if (!(argument instanceof BoolArgumentType) && mapping.getValue().right().isPresent()) {
                throw new IllegalStateException("Expected integer value for game rule '" + mapping.getKey() + "' but got boolean");
            }
        }
    }
}
