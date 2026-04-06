package dev.ftb.packcompanion.features.forcedgamerule;

import com.mojang.datafixers.util.Either;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ForcedGameRulesFeature extends Feature.Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForcedGameRulesFeature.class);

    public ForcedGameRulesFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);
    }

    @SuppressWarnings("unchecked")
    public void onLevelLoad(ServerStartedEvent event) {
        var forcedGameRules = PCCommonConfig.GAME_RULE_MAPPING.get();

        MinecraftServer server = event.getServer();
        for (Map.Entry<String, Either<Integer, Boolean>> mapping : forcedGameRules.entrySet()) {
            var id = Identifier.tryParse(mapping.getKey());
            var value = mapping.getValue();

            if (id == null) {
                LOGGER.warn("Invalid game rule identifier: '{}'", mapping.getKey());
                continue;
            }

            var ruleHolder = BuiltInRegistries.GAME_RULE.get(id);
            if (ruleHolder.isEmpty()) {
                LOGGER.warn("Unknown game rule '{}' in config", mapping.getKey());
                continue;
            }

            GameRules gameRules = server.getGameRules();
            var rule = ruleHolder.get().value();

            if (rule.valueClass().isInstance(Integer.class)) {
                gameRules.set((GameRule<Integer>) rule, value.left().orElseThrow(), server);
            } else if (rule.valueClass().isInstance(Boolean.class)) {
                gameRules.set((GameRule<Boolean>) rule, value.right().orElseThrow(), server);
            } else {
                LOGGER.warn("Unsupported game rule type for '{}'", mapping.getKey());
            }
        }
    }
}
