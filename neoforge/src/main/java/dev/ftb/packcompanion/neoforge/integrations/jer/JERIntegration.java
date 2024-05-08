package dev.ftb.packcompanion.neoforge.integrations.jer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.packcompanion.PackCompanionExpectPlatform;
import dev.ftb.packcompanion.api.PackCompanionAPI;
import jeresources.api.IJERAPI;
import jeresources.api.IJERPlugin;
import jeresources.api.JERPlugin;
import jeresources.util.LogHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@JERPlugin
public class JERIntegration implements IJERPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JERIntegration.class);

    @Override
    public void receive(IJERAPI ijerapi) {
        LOGGER.info("Loading Better Loot JER Integration");
        var extendedLootJsonPath = PackCompanionExpectPlatform.getGameDirectory().resolve("data/" + PackCompanionAPI.MOD_ID + "/jer_extended_loot.json");

        if (Files.notExists(extendedLootJsonPath)) {
            LOGGER.warn("jer_extended_loot.json does not exist, skipping");
            return;
        }

        try {
            String s = Files.readString(extendedLootJsonPath);
            JsonElement jsonElement = JsonParser.parseString(s);
            List<Tables> tables = Tables.LIST_CODEC.decode(JsonOps.INSTANCE, jsonElement)
                    .getOrThrow(false, LOGGER::error)
                    .getFirst();

            for (var table : tables) {
                ijerapi.getDungeonRegistry().registerCategory(table.name(), table.displayName());
                for (String chest : table.chests()) {
                    //ijerapi.getDungeonRegistry().registerChest(table.name(), new ResourceLocation(chest));
                    // Use reflection to call registerChest
                    try {
                        ijerapi.getDungeonRegistry().getClass().getMethod("registerChest", String.class, ResourceLocation.class)
                                .invoke(ijerapi.getDungeonRegistry(), table.name(), new ResourceLocation(chest));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        LOGGER.error("Failed to register chest: " + chest, e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load jer_extended_loot.json", e);
        }
    }

    public record Tables(String name, String displayName, List<String> chests) {
        public static final Codec<Tables> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(Codec.STRING.fieldOf("name").forGetter(Tables::name),
                                Codec.STRING.fieldOf("displayName").forGetter(Tables::displayName),
                                Codec.STRING.listOf().fieldOf("chests").forGetter(Tables::chests))
                        .apply(inst, Tables::new));

        public static final Codec<List<Tables>> LIST_CODEC = CODEC.listOf();
    }

    /**
     * \@JER Devs: Please fix this, so I don't have to <3
     */
    public static void loadJerPlugins(IJERAPI instance) {
        Type pluginAnnotation = Type.getType(JERPlugin.class);
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        for (ModFileScanData scanData : allScanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for (ModFileScanData.AnnotationData a : annotations) {
                if (Objects.equals(a.annotationType(), pluginAnnotation)) {
                    try {
                        Class<?> clazz = Class.forName(a.clazz().getClassName());
                        IJERPlugin plugin = (IJERPlugin) clazz.getDeclaredConstructor().newInstance();
                        plugin.receive(instance);
                    } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException
                             | InstantiationException | InvocationTargetException e) {
                        LogHelper.warn("Failed to set: {}" + a.clazz().getClassName() + "." + a.memberName());
                    }
                }
            }
        }
    }
}
