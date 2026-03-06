package com.ppuetate.koksanchors.network;

import com.ppuetate.koksanchors.config.AnchorConfigManager;

/**
 * Tracker de debounce client-side para interacciones con Respawn Anchors.
 *
 * Mantiene timestamps de la ultima colocacion y carga para evitar
 * spam de paquetes cuando el jugador hace click rapido.
 *
 * NOTA: Este debounce es client-side. El servidor tiene su propia
 * validacion temporal via los mixins server-side.
 */
public final class AnchorDebounceTracker {

    private static long lastPlacementTime = 0L;
    private static long lastChargeTime = 0L;

    private AnchorDebounceTracker() {}

    /**
     * Verifica si se permite una nueva colocacion de anchor.
     *
     * @return true si ha pasado suficiente tiempo desde la ultima colocacion
     */
    public static boolean canPlace() {
        long now = System.currentTimeMillis();
        int debounceMs = AnchorConfigManager.current().placementDebounceMs();

        if (debounceMs <= 0) return true;

        if (now - lastPlacementTime >= debounceMs) {
            lastPlacementTime = now;
            return true;
        }

        return false;
    }

    /**
     * Verifica si se permite una nueva carga con glowstone.
     *
     * @return true si ha pasado suficiente tiempo desde la ultima carga
     */
    public static boolean canCharge() {
        long now = System.currentTimeMillis();
        int debounceMs = AnchorConfigManager.current().chargeDebounceMs();

        if (debounceMs <= 0) return true;

        if (now - lastChargeTime >= debounceMs) {
            lastChargeTime = now;
            return true;
        }

        return false;
    }

    /**
     * Marca manualmente el timestamp de colocacion.
     * Usado cuando el debounce se aplica desde el mixin.
     */
    public static void markPlacement() {
        lastPlacementTime = System.currentTimeMillis();
    }

    /**
     * Marca manualmente el timestamp de carga.
     * Usado cuando el debounce se aplica desde el mixin.
     */
    public static void markCharge() {
        lastChargeTime = System.currentTimeMillis();
    }

    /**
     * Reset de timestamps (util para reconexiones o cambio de mundo).
     */
    public static void reset() {
        lastPlacementTime = 0L;
        lastChargeTime = 0L;
    }
}
