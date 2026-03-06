package com.ppuetate.koksanchors;

import com.ppuetate.koksanchors.network.AnchorClientNetworking;
import net.fabricmc.api.ClientModInitializer;

/**
 * Entrypoint client-side de KoKs Anchors.
 *
 * Registra:
 * - Handlers de paquetes S2C (confirmacion/rechazo de colocacion/carga)
 * - Sistema de debounce client-side para colocacion y carga
 * - Logica de reversion de BlockState en caso de rechazo del servidor
 */
public class KoKsAnchorsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Registrar handlers client-side para paquetes S2C
        AnchorClientNetworking.registerClientHandlers();

        KoKsAnchors.LOGGER.info("[KoKs Anchors] Client-side inicializado");
    }
}
