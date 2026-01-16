package dev.ftb.packcompanion.features.villager;

import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;

public class NoWanderingTraderInvisPotions extends Feature.Server {
    public NoWanderingTraderInvisPotions(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
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
