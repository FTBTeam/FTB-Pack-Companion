package dev.ftb.packcompanion.features.structures;

import dev.ftb.packcompanion.config.PCServerConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JigsawRotationMapper {
    public static final Logger LOGGER = LoggerFactory.getLogger(JigsawRotationMapper.class);

    private static final Map<String,Rotation> rotByName = Util.make(new HashMap<>(), map -> {
        for (var r : Rotation.values()) {
            map.put(r.getSerializedName(), r);
        }
    });

    private static final Lazy<Map<ResourceLocation,Rotation>> rotationMap
            = Lazy.of(JigsawRotationMapper::loadRotationMap);

    public static Optional<Rotation> getRotationOverride(@Nullable ResourceKey<StructureTemplatePool> startPoolKey) {
        if (startPoolKey == null) {
            return Optional.empty();
        }

        Rotation rot = rotationMap.get().get(startPoolKey.location());
        if (rot != null) {
            LOGGER.debug("forced rotation for structure template pool {} to {}", startPoolKey.location(), rot);
        }
        return Optional.ofNullable(rot);
    }

    private static Map<ResourceLocation, Rotation> loadRotationMap() {
        Map<ResourceLocation, Rotation> res = new HashMap<>();
        PCServerConfig.STRUCTURE_ROTATION_OVERRIDE.get().forEach((poolStr, rotStr) -> {
            try {
                ResourceLocation poolId = ResourceLocation.parse(poolStr);
                Rotation rot = rotByName.get(rotStr);
                if (rot == null) throw new IllegalArgumentException();
                res.put(poolId, rot);
            } catch (ResourceLocationException e) {
                LOGGER.error("invalid template pool ID {}, skipping", poolStr);
            } catch (IllegalArgumentException e) {
                LOGGER.error("invalid rotation {}, skipping", rotStr);
            }
        });

        return res;
    }
}
