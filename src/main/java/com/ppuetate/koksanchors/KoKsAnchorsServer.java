package com.ppuetate.koksanchors;

import com.ppuetate.koksanchors.network.AnchorNetworking;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Entrypoint server-side de KoKs Anchors.
 *
 * Registra:
 * - Handlers de paquetes C2S (solicitudes de colocacion/carga)
 * - Validacion server-side autoritativa
 * - Envio de confirmaciones/rechazos al cliente
 */
public class KoKsAnchorsServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // Registrar handlers server-side para paquetes C2S
        AnchorNetworking.registerServerHandlers();

        KoKsAnchors.LOGGER.info("[KoKs Anchors] Server-side inicializado");
    }
}
