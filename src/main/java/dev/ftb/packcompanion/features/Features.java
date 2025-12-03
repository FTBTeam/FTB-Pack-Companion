package dev.ftb.packcompanion.features;

import dev.ftb.packcompanion.features.buffs.MobEntityBuff;
import dev.ftb.packcompanion.features.spawners.SpawnerManager;
import dev.ftb.packcompanion.features.triggerblock.TriggerBlockFeature;

import java.util.ArrayList;
import java.util.List;

public class Features {
    public static final Features INSTANCE = new Features();

    List<CommonFeature> commonFeatures = new ArrayList<>();
    List<ServerFeature> serverFeatures = new ArrayList<>();

    private Features() {
        this.serverFeatures.addAll(List.of(
            new MobEntityBuff(),
            new SpawnerManager()
        ));

        this.commonFeatures.addAll(List.of(
                new TriggerBlockFeature()
        ));
    }

    public List<CommonFeature> getCommonFeatures() {
        return commonFeatures;
    }

    public List<ServerFeature> getServerFeatures() {
        return serverFeatures;
    }
}
