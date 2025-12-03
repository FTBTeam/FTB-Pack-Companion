package dev.ftb.packcompanion.features.buffs;

import com.google.common.base.Suppliers;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.features.ServerFeature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;

import java.util.UUID;
import java.util.function.Supplier;

public class MobEntityBuff extends ServerFeature {
    private static final UUID MODIFIER_UUID = UUID.fromString("a07a9434-d6c2-44f1-b5eb-394da41c9f9f");

    private static Supplier<AttributeModifier> MODIFIER = Suppliers.memoize(() -> {
        var multiplierValue = PCServerConfig.MODIFY_MOB_BASE_HEALTH.get();
        return new AttributeModifier(MODIFIER_UUID, "ftbpc:mob_entity_health_buff", multiplierValue, AttributeModifier.Operation.MULTIPLY_BASE);
    });

    @Override
    public void initialize() {
        // On entity load
        EntityEvent.ADD.register((entity, world) -> {
            if (entity instanceof Enemy && entity instanceof Mob mob) {
                AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
                if (attribute != null && attribute.getModifier(MODIFIER_UUID) == null) {
                    attribute.addPermanentModifier(MODIFIER.get());
                    ((Mob) entity).setHealth(((Mob) entity).getMaxHealth());
                }
            }

            return EventResult.pass();
        });
    }

    @Override
    public boolean isDisabled() {
        return PCServerConfig.MODIFY_MOB_BASE_HEALTH.get() == 0.0D;
    }
}
