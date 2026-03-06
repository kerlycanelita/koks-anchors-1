package com.ppuetate.koksanchors.config;

import com.ppuetate.koksanchors.KoKsAnchors;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Integracion con Mod Menu.
 *
 * Intenta usar Cloth Config para la pantalla de configuracion bonita.
 * Si Cloth Config no esta disponible, usa la pantalla fallback vanilla.
 */
public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                return AnchorClothConfigScreen.create(parent);
            } catch (Throwable throwable) {
                KoKsAnchors.LOGGER.warn(
                    "[KoKs Anchors] Cloth Config no disponible; usando pantalla fallback",
                    throwable
                );
                return new AnchorConfigScreen(parent);
            }
        };
    }
}
