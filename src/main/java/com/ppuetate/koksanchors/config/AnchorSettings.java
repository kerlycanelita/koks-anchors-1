package com.ppuetate.koksanchors.config;

import net.minecraft.util.math.MathHelper;

/**
 * Configuracion de KoKs Anchors.
 *
 * Contiene todos los valores ajustables del mod:
 * - Debounce de colocacion (placement)
 * - Debounce de carga con glowstone (charge)
 * - Flags de optimizacion
 *
 * Todos los valores se sanitizan al cargar/guardar para evitar
 * configuraciones invalidas.
 */
public final class AnchorSettings {

    // ── Limites de debounce de colocacion ──
    public static final int MIN_PLACEMENT_DEBOUNCE_MS = 0;
    public static final int MAX_PLACEMENT_DEBOUNCE_MS = 500;
    public static final int DEFAULT_PLACEMENT_DEBOUNCE_MS = 50;

    // ── Limites de debounce de carga (glowstone) ──
    public static final int MIN_CHARGE_DEBOUNCE_MS = 0;
    public static final int MAX_CHARGE_DEBOUNCE_MS = 500;
    public static final int DEFAULT_CHARGE_DEBOUNCE_MS = 50;

    // ── Flags de optimizacion ──
    public static final boolean DEFAULT_ANTI_GHOST_ENABLED = true;
    public static final boolean DEFAULT_FAST_RENDER_UPDATE = true;
    public static final boolean DEFAULT_SUPPRESS_NEIGHBOR_UPDATES = true;

    // ── Campos ──
    private int placementDebounceMs = DEFAULT_PLACEMENT_DEBOUNCE_MS;
    private int chargeDebounceMs = DEFAULT_CHARGE_DEBOUNCE_MS;
    private boolean antiGhostEnabled = DEFAULT_ANTI_GHOST_ENABLED;
    private boolean fastRenderUpdate = DEFAULT_FAST_RENDER_UPDATE;
    private boolean suppressNeighborUpdates = DEFAULT_SUPPRESS_NEIGHBOR_UPDATES;

    public AnchorSettings copy() {
        AnchorSettings copy = new AnchorSettings();
        copy.placementDebounceMs = this.placementDebounceMs;
        copy.chargeDebounceMs = this.chargeDebounceMs;
        copy.antiGhostEnabled = this.antiGhostEnabled;
        copy.fastRenderUpdate = this.fastRenderUpdate;
        copy.suppressNeighborUpdates = this.suppressNeighborUpdates;
        copy.sanitize();
        return copy;
    }

    public void resetToDefaults() {
        this.placementDebounceMs = DEFAULT_PLACEMENT_DEBOUNCE_MS;
        this.chargeDebounceMs = DEFAULT_CHARGE_DEBOUNCE_MS;
        this.antiGhostEnabled = DEFAULT_ANTI_GHOST_ENABLED;
        this.fastRenderUpdate = DEFAULT_FAST_RENDER_UPDATE;
        this.suppressNeighborUpdates = DEFAULT_SUPPRESS_NEIGHBOR_UPDATES;
    }

    public void sanitize() {
        this.placementDebounceMs = MathHelper.clamp(
            this.placementDebounceMs, MIN_PLACEMENT_DEBOUNCE_MS, MAX_PLACEMENT_DEBOUNCE_MS
        );
        this.chargeDebounceMs = MathHelper.clamp(
            this.chargeDebounceMs, MIN_CHARGE_DEBOUNCE_MS, MAX_CHARGE_DEBOUNCE_MS
        );
    }

    // ── Accessors ──

    public int placementDebounceMs() { return placementDebounceMs; }
    public void placementDebounceMs(int value) { this.placementDebounceMs = value; }

    public int chargeDebounceMs() { return chargeDebounceMs; }
    public void chargeDebounceMs(int value) { this.chargeDebounceMs = value; }

    public boolean antiGhostEnabled() { return antiGhostEnabled; }
    public void antiGhostEnabled(boolean value) { this.antiGhostEnabled = value; }

    public boolean fastRenderUpdate() { return fastRenderUpdate; }
    public void fastRenderUpdate(boolean value) { this.fastRenderUpdate = value; }

    public boolean suppressNeighborUpdates() { return suppressNeighborUpdates; }
    public void suppressNeighborUpdates(boolean value) { this.suppressNeighborUpdates = value; }
}
