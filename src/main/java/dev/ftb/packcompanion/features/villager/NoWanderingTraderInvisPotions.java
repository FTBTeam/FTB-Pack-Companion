package dev.ftb.packcompanion.features.villager;

import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class NoWanderingTraderInvisPotions extends Feature.Server {
    public NoWanderingTraderInvisPotions(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (PCServerConfig.NO_WANDERING_TRADER_INVIS_POTIONS.get()
                && event.getLevel() instanceof ServerLevel
                && event.getEntity() instanceof WanderingTrader trader)
        {
            trader.goalSelector.removeAllGoals(goal -> goal instanceof UseItemGoal<?>);
        }
    }
}
