package dev.ftb.packcompanion.features.forcedgamerule;

import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class ForcedGameRulesFeature extends Feature.Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForcedGameRulesFeature.class);

    public ForcedGameRulesFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        MinecraftForge.EVENT_BUS.addListener(this::onLevelLoad);
    }

    public void onLevelLoad(ServerStartedEvent event) {
        var forcedGameRules = PCCommonConfig.GAME_RULE_MAPPING.get();

        MinecraftServer server = event.getServer();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(@Nonnull GameRules.Key<T> key, @Nonnull GameRules.Type<T> type) {
                if (forcedGameRules.containsKey(key.getId())) {
                    Tag value = forcedGameRules.get(key.getId());
                    if (value instanceof ByteTag byteTag) {
                        GameRules.BooleanValue rule = (GameRules.BooleanValue) server.getGameRules().getRule(key);
                        var asBoolean = byteTag.getAsByte() != 0;
                        if (rule.get() != asBoolean) {
                            LOGGER.info("Setting (bool) game rule '{}' to {}", key.getId(), asBoolean);
                            rule.set(asBoolean, server);
                        }
                    } else if (value instanceof IntTag intTag) {
                        GameRules.IntegerValue rule = (GameRules.IntegerValue) server.getGameRules().getRule(key);
                        if (rule.get() != intTag.getAsInt()) {
                            LOGGER.info("Setting (int) game rule '{}' to {}", key.getId(), intTag.getAsInt());
                            rule.set(intTag.getAsInt(), server);
                        }
                    }
                }
            }
        });
    }
}
