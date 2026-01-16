package dev.ftb.packcompanion.features.buffs;

import com.google.common.base.Suppliers;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;

import java.util.UUID;
import java.util.function.Supplier;

public class MobEntityBuffFeature extends Feature.Server {
    private static final UUID MODIFIER_UUID = UUID.fromString("a07a9434-d6c2-44f1-b5eb-394da41c9f9f");

    private static Supplier<AttributeModifier> MODIFIER = Suppliers.memoize(() -> {
        var multiplierValue = PCServerConfig.MODIFY_MOB_BASE_HEALTH.get();
        return new AttributeModifier(MODIFIER_UUID, "ftbpc:mob_entity_health_buff", multiplierValue, AttributeModifier.Operation.MULTIPLY_BASE);
    });

    public MobEntityBuffFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (PCServerConfig.MODIFY_MOB_BASE_HEALTH.get() == 0.0D) {
            return; // No need to register if the config is set to 0
        }

        MinecraftForge.EVENT_BUS.addListener(this::onEntityAdded);
    }


    public void onEntityAdded(EntityJoinLevelEvent event) {
        var entity = event.getEntity();

        if (entity instanceof Enemy && entity instanceof Mob mob) {
            AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
            if (attribute != null && attribute.getModifier(MODIFIER_UUID) == null) {
                attribute.addPermanentModifier(MODIFIER.get());
                ((Mob) entity).setHealth(((Mob) entity).getMaxHealth());
            }
        }
    }
}
