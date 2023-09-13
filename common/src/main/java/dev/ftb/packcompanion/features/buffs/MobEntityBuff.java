package dev.ftb.packcompanion.features.buffs;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.features.ServerFeature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;

public class MobEntityBuff extends ServerFeature {
    @Override
    public void initialize() {
        // On entity load
        EntityEvent.ADD.register((entity, world) -> {
            if (entity instanceof Enemy && entity instanceof Mob mob) {
                AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
                if (attribute != null) {
                    attribute.setBaseValue(attribute.getBaseValue() * PCServerConfig.MODIFY_MOB_BASE_HEALTH.get());
                    ((Mob) entity).setHealth((float) attribute.getBaseValue());
                }
            }

            return EventResult.pass();
        });
    }

    @Override
    public boolean isEnabled() {
        return PCServerConfig.MODIFY_MOB_BASE_HEALTH.get() > 0;
    }
}
