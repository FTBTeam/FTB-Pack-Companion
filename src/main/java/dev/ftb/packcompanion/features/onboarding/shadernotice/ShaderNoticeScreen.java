package dev.ftb.packcompanion.features.onboarding.shadernotice;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.packcompanion.config.PCCommonConfig;
import dev.ftb.packcompanion.integrations.iris.ShadersIntegration;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderNoticeScreen extends BaseScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderNoticeScreen.class);
    private final ShaderNotice shaderNotice;

    public ShaderNoticeScreen(ShaderNotice shaderNotice) {
        super();

        this.shaderNotice = shaderNotice;

        this.setWidth(300);
        this.setHeight(190);

        if (!ShadersIntegration.get().isAvailable()) {
            closeGui();
        }

        setPreviousScreen(null);
    }

    private Option shadersOffPanel;
    private Option shadersOnPanel;

    @Override
    public void addWidgets() {
        this.add(this.shadersOffPanel = new Option(this, Component.translatable("ftbpackcompanion.shaders_notice.no_shaders.title"), Component.translatable("ftbpackcompanion.shaders_notice.shaders_btn.disable"), Icons.CANCEL, Component.translatable("ftbpackcompanion.shaders_notice.no_shaders.description"), (accepted) -> {
            var isAvailable = ShadersIntegration.get().isAvailable();
            if (isAvailable) {
                ShadersIntegration.get().provider().disabledShaders();
            }

            this.shaderNotice.hasOnboarded.set(true);
            this.shaderNotice.shaderData.save();
            this.closeGui();
        }));

        this.add(this.shadersOnPanel = new Option(this, Component.translatable("ftbpackcompanion.shaders_notice.shaders.title"), Component.translatable("ftbpackcompanion.shaders_notice.shaders_btn.enable"), Icons.CHECK, Component.translatable("ftbpackcompanion.shaders_notice.shaders.description"), (accepted) -> {
            var isAvailable = ShadersIntegration.get().isAvailable();
            if (isAvailable) {
                ShadersIntegration.get().provider().applyShaderPack(PCCommonConfig.SHADER_PACK_TO_USE.get());
            }
            this.shaderNotice.hasOnboarded.set(true);
            this.shaderNotice.shaderData.save();
            this.closeGui();
        }).withAlt(true));
    }

    @Override
    public void alignWidgets() {
        this.shadersOffPanel.setSize(150, 190);
        this.shadersOffPanel.alignWidgets();

        this.shadersOnPanel.setSize(150, 190);
        this.shadersOnPanel.alignWidgets();

        this.align(WidgetLayout.HORIZONTAL);
    }

    @Override
    public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawForeground(graphics, theme, x, y, w, h);
        MutableComponent text = Component.translatable("ftbpackcompanion.shaders_notice.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA);
        int stringWidth = this.getTheme().getStringWidth(text);
        graphics.drawString(this.getTheme().getFont(), text, x + ((width - stringWidth) / 2), y - 16, Color4I.WHITE.rgb());
    }

    private static class Option extends Panel {
        private final Component title;
        private final Component description;
        private final Component btnText;

        private final Icon btnIcon;
        private final BooleanConsumer onSelect;
        private boolean isAlt = false;

        private TextField titleField;
        private PanelScrollBar scrollBar;
        private DescriptionPanel descriptionPanel;
        private Button acceptButton;

        public Option(Panel panel, Component title, Component btnText, Icon btnIcon, Component description, BooleanConsumer onSelect) {
            super(panel);

            this.title = title;
            this.onSelect = onSelect;
            this.description = description;
            this.btnText = btnText;
            this.btnIcon = btnIcon;
        }

        public Option withAlt(boolean withAlt) {
            this.isAlt = withAlt;
            return this;
        }

        @Override
        public void addWidgets() {
            this.titleField = new TextField(this);
            this.titleField.setText(this.title.copy().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE));
            this.add(this.titleField);

            this.add(this.descriptionPanel = new DescriptionPanel(this, this.description));

            this.add(this.acceptButton = SimpleTextButton.create(this, this.btnText, btnIcon, (mouseButton) -> {
                this.onSelect.accept(true);
            }));

            this.add(this.scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, this.descriptionPanel));
        }

        @Override
        public void alignWidgets() {
            this.titleField.setPosAndSize(5, 0, this.width - 10, 12);
            this.acceptButton.setSize(this.width - 6, 18);
            this.acceptButton.setPos(3, 0); // This will be overridden?

            this.align(new WidgetLayout.Vertical(5, 5, 0));

            this.descriptionPanel.alignWidgets();

            scrollBar.setPosAndSize(this.descriptionPanel.getPosX() + this.descriptionPanel.getWidth() - 6, this.descriptionPanel.getPosY(), 6, this.descriptionPanel.getHeight());
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            if (this.isAlt) {
                Color4I.WHITE.withAlphaf(.1f).draw(graphics, x, y, w, h);
            }
        }
    }

    private static class DescriptionPanel extends Panel {
        private final Component description;
        private TextField descriptionField;

        public DescriptionPanel(Panel panel, Component description) {
            super(panel);
            this.setSize(148, 142);
            this.description = description;
        }

        @Override
        public void addWidgets() {
            this.add(this.descriptionField = new TextField(this));
            this.descriptionField.setText(this.description);
        }

        @Override
        public void alignWidgets() {
            this.descriptionField.setSize(this.width - 10, this.height);
            this.descriptionField.setPos(5, 0);
            this.descriptionField.setMinWidth(width - 10);
            this.descriptionField.setMaxWidth(width - 10);
            this.descriptionField.reflow();
        }
    }
}
