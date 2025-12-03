package dev.ftb.packcompanion.features.triggerblock;

import dev.architectury.event.events.common.TickEvent;
import dev.ftb.packcompanion.core.DataGatherCollector;
import dev.ftb.packcompanion.features.CommonFeature;

public class TriggerBlockFeature extends CommonFeature {
    @Override
    public void initialize() {
        TickEvent.ServerLevelTick.SERVER_LEVEL_POST.register((level) -> TriggerBlockController.INSTANCE.onTick());
    }

    @Override
    public void onDataGather(DataGatherCollector collector) {
        DataGatherCollector.TranslationCollector translations = collector.translationCollector();
        translations.addBlock(TRIGGER_BLOCK, "Player Trigger");

        collector.addBlockStateProvider(provider -> {
            provider.simpleBlock(TRIGGER_BLOCK.get());
        });

        collector.addItemModelProvider(provider -> {
            provider.simpleBlockItem(TRIGGER_BLOCK.get());
        });
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
