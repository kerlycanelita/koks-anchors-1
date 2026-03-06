package com.ppuetate.koksanchors.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Pantalla de configuracion usando Cloth Config API.
 *
 * Categorias:
 * - Debounce: Controles de frecuencia para colocacion y carga
 * - Optimizacion: Flags de anti-ghost, render rapido, neighbor updates
 * - Info: Autores e informacion tecnica
 */
public final class AnchorClothConfigScreen {

    private AnchorClothConfigScreen() {}

    public static Screen create(Screen parent) {
        AnchorSettings draft = AnchorConfigManager.snapshot();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("KoKs Anchors - Configuracion"))
            .setSavingRunnable(() -> {
                draft.sanitize();
                AnchorConfigManager.updateAndSave(draft);
            });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        buildDebounceCategory(builder, entryBuilder, draft);
        buildOptimizationCategory(builder, entryBuilder, draft);
        buildInfoCategory(builder, entryBuilder);

        return builder.build();
    }

    private static void buildDebounceCategory(
        ConfigBuilder builder,
        ConfigEntryBuilder entryBuilder,
        AnchorSettings draft
    ) {
        ConfigCategory category = builder.getOrCreateCategory(Text.literal("Debounce"));

        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Controla la frecuencia de interacciones con Respawn Anchors.")
        ).build());

        // Debounce de colocacion
        category.addEntry(entryBuilder.startIntSlider(
                Text.literal("Debounce colocacion (ms)"),
                draft.placementDebounceMs(),
                AnchorSettings.MIN_PLACEMENT_DEBOUNCE_MS,
                AnchorSettings.MAX_PLACEMENT_DEBOUNCE_MS
            ).setDefaultValue(AnchorSettings.DEFAULT_PLACEMENT_DEBOUNCE_MS)
            .setSaveConsumer(draft::placementDebounceMs)
            .setTooltip(
                Text.literal("Tiempo minimo entre colocaciones de Respawn Anchor."),
                Text.literal("Evita ghost placements por spam click."),
                Text.literal("0 = sin debounce (vanilla behavior).")
            )
            .build());

        // Debounce de carga
        category.addEntry(entryBuilder.startIntSlider(
                Text.literal("Debounce carga glowstone (ms)"),
                draft.chargeDebounceMs(),
                AnchorSettings.MIN_CHARGE_DEBOUNCE_MS,
                AnchorSettings.MAX_CHARGE_DEBOUNCE_MS
            ).setDefaultValue(AnchorSettings.DEFAULT_CHARGE_DEBOUNCE_MS)
            .setSaveConsumer(draft::chargeDebounceMs)
            .setTooltip(
                Text.literal("Tiempo minimo entre cargas con glowstone."),
                Text.literal("Evita multiples envios de paquetes por spam click."),
                Text.literal("0 = sin debounce (vanilla behavior).")
            )
            .build());
    }

    private static void buildOptimizationCategory(
        ConfigBuilder builder,
        ConfigEntryBuilder entryBuilder,
        AnchorSettings draft
    ) {
        ConfigCategory category = builder.getOrCreateCategory(Text.literal("Optimizacion"));

        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Flags de optimizacion avanzada para Respawn Anchors.")
        ).build());

        // Anti-ghost
        category.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Anti-Ghost"),
                draft.antiGhostEnabled()
            ).setDefaultValue(AnchorSettings.DEFAULT_ANTI_GHOST_ENABLED)
            .setSaveConsumer(draft::antiGhostEnabled)
            .setTooltip(
                Text.literal("Elimina ghost blocks al colocar/romper anchors."),
                Text.literal("El cliente espera confirmacion del servidor antes de"),
                Text.literal("finalizar el render del bloque.")
            )
            .build());

        // Fast render update
        category.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Render update rapido"),
                draft.fastRenderUpdate()
            ).setDefaultValue(AnchorSettings.DEFAULT_FAST_RENDER_UPDATE)
            .setSaveConsumer(draft::fastRenderUpdate)
            .setTooltip(
                Text.literal("Fuerza actualizacion inmediata del chunk section"),
                Text.literal("al cambiar el BlockState del anchor."),
                Text.literal("Reduce lag visual en la transicion de estados.")
            )
            .build());

        // Suppress neighbor updates
        category.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Suprimir neighbor updates innecesarios"),
                draft.suppressNeighborUpdates()
            ).setDefaultValue(AnchorSettings.DEFAULT_SUPPRESS_NEIGHBOR_UPDATES)
            .setSaveConsumer(draft::suppressNeighborUpdates)
            .setTooltip(
                Text.literal("Reduce las notificaciones de bloque vecino al cargar"),
                Text.literal("el anchor con glowstone. Mejora TPS en situaciones"),
                Text.literal("de spam-charge masivo.")
            )
            .build());
    }

    private static void buildInfoCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = builder.getOrCreateCategory(Text.literal("Info"));

        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Autor: Zymekoh")
        ).build());
        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Proyecto: KoKs Anchors - KoHs Development")
        ).build());
        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Tipo: Optimizacion Respawn Anchor (Client + Server)")
        ).build());
        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Sincronizacion: Debounce client-side + validacion server-side")
        ).build());
        category.addEntry(entryBuilder.startTextDescription(
            Text.literal("Compatible con: Crystal PvP, Anchor PvP, vanilla servers con mod")
        ).build());
    }
}
