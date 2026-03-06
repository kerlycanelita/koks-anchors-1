package com.ppuetate.koksanchors;

import com.ppuetate.koksanchors.config.AnchorConfigManager;
import com.ppuetate.koksanchors.network.AnchorNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KoKs Anchors - Advanced Respawn Anchor Optimization for Crystal PvP
 *
 * Este mod optimiza la colocacion y carga de Respawn Anchors eliminando
 * ghosting visual/logico y reduciendo la latencia de interaccion.
 *
 * ARQUITECTURA:
 * - main: Inicializacion comun, config, registro de payloads de red
 * - client: Debounce client-side, prediccion de colocacion, render sync
 * - server: Validacion autoritativa, confirmacion de BlockState, anti-ghost
 *
 * SINCRONIZACION:
 * El flujo de colocacion/carga sigue el patron:
 * 1. Cliente aplica prediccion optimista (coloca visualmente)
 * 2. Servidor valida y confirma/rechaza
 * 3. Si el servidor rechaza, el cliente revierte el BlockState
 * 4. Si el servidor confirma, el BlockState ya esta correcto
 *
 * @author Zymekoh
 */
public class KoKsAnchors implements ModInitializer {

    public static final String MOD_ID = "koks-anchors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Cargar configuracion
        AnchorConfigManager.load();

        // Registrar payloads de red (IDs compartidos client+server)
        AnchorNetworking.registerPayloads();

        LOGGER.info("[KoKs Anchors] Mod inicializado - Anchor optimization activa");
        LOGGER.info("[KoKs Anchors] Debounce colocacion: {}ms", AnchorConfigManager.current().placementDebounceMs());
        LOGGER.info("[KoKs Anchors] Debounce carga glowstone: {}ms", AnchorConfigManager.current().chargeDebounceMs());
    }
}
