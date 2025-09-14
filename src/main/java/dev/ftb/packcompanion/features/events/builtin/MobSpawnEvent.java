package dev.ftb.packcompanion.features.events.builtin;

import dev.ftb.packcompanion.features.events.Event;
import dev.ftb.packcompanion.features.events.EventConfig;
import dev.ftb.packcompanion.features.events.EventContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class MobSpawnEvent extends Event {
    private final double chance;

    private final String mobType;
    @Nullable
    private final Consumer<Entity> entityMutator;

    private final Lazy<EntityType<?>> entityResolver;

    public MobSpawnEvent(ResourceLocation id, String name, String description, double chance, EventConfig config, String mobType, @Nullable Consumer<Entity> entityMutator) {
        super(id, name, description, config);
        this.chance = chance;

        this.mobType = mobType;
        this.entityMutator = entityMutator;

        this.entityResolver = Lazy.of(() -> {
            // This is a defaulted registry so this could potentially return the default value if the mobType is invalid
            return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.mobType));
        });
    }

    @Override
    public void action(EventContext context) {
        ServerLevel level = context.level();
        Entity createdEntity = this.entityResolver.get().create(level);
        if (createdEntity == null) {
            return;
        }

        if (context.spawnPos() == null) {
            throw new IllegalStateException("MobSpawnEvent requires a spawn position in the context");
        }

        createdEntity.setPos(context.spawnPos().getX() + 0.5, context.spawnPos().getY(), context.spawnPos().getZ() + 0.5);
        if (this.entityMutator != null) {
            this.entityMutator.accept(createdEntity);
        }

        level.addFreshEntity(createdEntity);
    }

    @Override
    public double chance() {
        return chance;
    }
}
