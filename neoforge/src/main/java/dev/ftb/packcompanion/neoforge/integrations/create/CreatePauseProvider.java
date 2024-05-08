package dev.ftb.packcompanion.neoforge.integrations.create;

//import com.simibubi.create.infrastructure.gui.OpenCreateMenuButton;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseProvider;
import dev.ftb.packcompanion.api.client.pause.AdditionalPauseTarget;
import dev.ftb.packcompanion.api.client.pause.ScreenHolder;
import dev.ftb.packcompanion.api.client.pause.ScreenWidgetCollection;

public class CreatePauseProvider implements AdditionalPauseProvider {
    @Override
    public ScreenWidgetCollection init(AdditionalPauseTarget target, ScreenHolder screen, int x, int y) {
        return ScreenWidgetCollection.create();
//                .addRenderableWidget(new OpenCreateMenuButton(x - 20, y));
    }
}
