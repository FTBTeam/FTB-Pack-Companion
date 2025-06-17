package dev.ftb.packcompanion.features.buffs;

import com.google.common.base.Suppliers;
import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.config.PCServerConfig;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MobEntityBuffFeature extends Feature.Server {
    private static final UUID MODIFIER_UUID = UUID.fromString("a07a9434-d6c2-44f1-b5eb-394da41c9f9f");

    private static final ResourceLocation MOB_ENTITY_HEALTH_BUFF_ID = PackCompanion.id("mob_entity_health_buff");

    private static final Supplier<AttributeModifier> MODIFIER = Suppliers.memoize(() -> {
        var multiplierValue = PCServerConfig.MODIFY_MOB_BASE_HEALTH.get();
        return new AttributeModifier(MOB_ENTITY_HEALTH_BUFF_ID, multiplierValue, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    });

    public MobEntityBuffFeature(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        if (PCServerConfig.MODIFY_MOB_BASE_HEALTH.get() == 0.0D) {
            return; // No need to register if the config is set to 0
        }

        NeoForge.EVENT_BUS.addListener(this::onEntityAdded);
    }

    public void onEntityAdded(EntityJoinLevelEvent event) {
        var entity = event.getEntity();

        if (entity instanceof Enemy && entity instanceof Mob mob) {
            AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
            if (attribute != null && attribute.getModifier(MOB_ENTITY_HEALTH_BUFF_ID) == null) {
                attribute.addPermanentModifier(MODIFIER.get());
                mob.setHealth(mob.getMaxHealth());
            }
        }
    }
}
