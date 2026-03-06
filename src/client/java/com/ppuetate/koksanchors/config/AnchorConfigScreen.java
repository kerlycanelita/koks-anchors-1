package com.ppuetate.koksanchors.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Pantalla fallback de configuracion sin Cloth Config.
 *
 * Provee sliders basicos para debounce y toggles para flags de optimizacion.
 */
public final class AnchorConfigScreen extends Screen {

    private static final Text TITLE = Text.literal("KoKs Anchors - Configuracion");
    private static final int COLOR_TEXT = 0xE0E0E0;
    private static final int COLOR_ACCENT = 0x73D274;

    private final Screen parent;
    private final AnchorSettings draft;

    public AnchorConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.draft = AnchorConfigManager.snapshot();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(340, this.width - 24);
        int left = (this.width - contentWidth) / 2;
        int y = 30;

        // Debounce colocacion
        this.addDrawableChild(new IntSettingSlider(
            left, y, contentWidth, Text.literal("Debounce colocacion"),
            draft.placementDebounceMs(),
            AnchorSettings.MIN_PLACEMENT_DEBOUNCE_MS,
            AnchorSettings.MAX_PLACEMENT_DEBOUNCE_MS,
            draft::placementDebounceMs, "ms"
        ));
        y += 26;

        // Debounce carga
        this.addDrawableChild(new IntSettingSlider(
            left, y, contentWidth, Text.literal("Debounce carga glowstone"),
            draft.chargeDebounceMs(),
            AnchorSettings.MIN_CHARGE_DEBOUNCE_MS,
            AnchorSettings.MAX_CHARGE_DEBOUNCE_MS,
            draft::chargeDebounceMs, "ms"
        ));
        y += 26;

        // Toggle: Anti-ghost
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Anti-Ghost: " + (draft.antiGhostEnabled() ? "ON" : "OFF")),
                button -> {
                    draft.antiGhostEnabled(!draft.antiGhostEnabled());
                    button.setMessage(Text.literal("Anti-Ghost: " + (draft.antiGhostEnabled() ? "ON" : "OFF")));
                }
            ).dimensions(left, y, contentWidth, 20).build()
        );
        y += 26;

        // Toggle: Fast render
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Fast Render Update: " + (draft.fastRenderUpdate() ? "ON" : "OFF")),
                button -> {
                    draft.fastRenderUpdate(!draft.fastRenderUpdate());
                    button.setMessage(Text.literal("Fast Render Update: " + (draft.fastRenderUpdate() ? "ON" : "OFF")));
                }
            ).dimensions(left, y, contentWidth, 20).build()
        );
        y += 26;

        // Toggle: Suppress neighbor updates
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Suprimir Neighbor Updates: " + (draft.suppressNeighborUpdates() ? "ON" : "OFF")),
                button -> {
                    draft.suppressNeighborUpdates(!draft.suppressNeighborUpdates());
                    button.setMessage(Text.literal("Suprimir Neighbor Updates: " + (draft.suppressNeighborUpdates() ? "ON" : "OFF")));
                }
            ).dimensions(left, y, contentWidth, 20).build()
        );

        // Botones de abajo
        int bottomY = this.height - 28;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Guardar y volver"), button -> saveAndClose())
                .dimensions(left, bottomY, 130, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Cancelar"), button -> close())
                .dimensions(left + 134, bottomY, 80, 20).build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Restablecer"), button -> {
                draft.resetToDefaults();
                clearAndInit();
            }).dimensions(left + contentWidth - 92, bottomY, 92, 20).build()
        );
    }

    private void saveAndClose() {
        draft.sanitize();
        AnchorConfigManager.updateAndSave(draft);
        close();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderBackground(context, mouseX, mouseY, deltaTicks);
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    /**
     * Slider para valores enteros (milisegundos de debounce).
     */
    private static final class IntSettingSlider extends SliderWidget {
        private final Text label;
        private final int min;
        private final int max;
        private final IntConsumer setter;
        private final String suffix;

        private IntSettingSlider(
            int x, int y, int width, Text label,
            int currentValue, int min, int max,
            IntConsumer setter, String suffix
        ) {
            super(x, y, width, 20, Text.empty(), normalize(currentValue, min, max));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.suffix = suffix;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int actual = denormalize(this.value);
            this.setMessage(Text.literal(this.label.getString() + ": " + actual + this.suffix));
        }

        @Override
        protected void applyValue() {
            int actual = denormalize(this.value);
            this.value = normalize(actual, this.min, this.max);
            this.setter.accept(actual);
            this.updateMessage();
        }

        private int denormalize(double normalized) {
            return MathHelper.clamp(
                (int) Math.round(this.min + (this.max - this.min) * normalized),
                this.min, this.max
            );
        }

        private static double normalize(int actual, int min, int max) {
            if (max <= min) return 0.0;
            return MathHelper.clamp((double)(actual - min) / (max - min), 0.0, 1.0);
        }
    }
}
