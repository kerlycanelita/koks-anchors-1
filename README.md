# KoKs Anchors

Mod Fabric para Minecraft **1.21.10** que optimiza el uso de **Respawn Anchors** en escenarios de PvP (Anchor/Crystal), reduciendo desincronizacion visual y spam de interacciones.

## Estado del proyecto

KoKs Anchors es una **beta descontinuada** y **ya no recibira actualizaciones**.

Este repositorio se mantiene solo como referencia historica. Las funciones de esta beta ya fueron integradas en **KoHs Mods Suite**, que es la version final recomendada.

Si buscas soporte, mejoras o nuevas versiones, usa KoHs Mods Suite en lugar de este proyecto.

## Que hace el proyecto

- Aplica **debounce client-side** en colocacion de Respawn Anchor.
- Aplica **debounce client-side** en carga con Glowstone.
- Aplica **validacion server-side** para rechazar spam de interacciones demasiado rapido.
- Sincroniza estado real del anchor (cargas) con payloads S2C `anchor_confirm` y `anchor_reject`.
- Incluye pantalla de configuracion con **Mod Menu + Cloth Config** (si estan presentes).
- Incluye pantalla fallback vanilla si Cloth Config no esta disponible.

## Arquitectura tecnica

- Entrada comun: `com.ppuetate.koksanchors.KoKsAnchors`
- Entrada cliente: `com.ppuetate.koksanchors.KoKsAnchorsClient`
- Mixins servidor: `ServerAnchorPlacementMixin` y `ServerAnchorInteractionMixin`
- Mixins cliente: `ClientBlockPlacementMixin` y `ClientAnchorRenderMixin`
- Red: `AnchorNetworking` (payload IDs + envio S2C)
- Red cliente: `AnchorClientNetworking` (aplica confirm/reject en cliente)
- Config persistente: `AnchorConfigManager` en `config/koks-anchors.json`

## Configuracion disponible

- `placementDebounceMs` (default: `50`, rango: `0-500`)
- `chargeDebounceMs` (default: `50`, rango: `0-500`)
- `antiGhostEnabled` (default: `true`)
- `fastRenderUpdate` (default: `true`)
- `suppressNeighborUpdates` (default: `true`)

## Requisitos

- JDK **21**
- Windows, Linux o macOS
- Conexion a internet para descargar dependencias Gradle la primera vez

## Compilacion

### Windows (PowerShell / CMD)

```bat
.\gradlew.bat clean build
```

### Linux / macOS

```bash
./gradlew clean build
```

Artefactos generados:

- `build/libs/koks-anchors-<version>.jar`
- `build/libs/koks-anchors-<version>-sources.jar`

## Ejecucion para desarrollo

```bash
./gradlew runClient
./gradlew runServer
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

## Versiones del proyecto

- Minecraft: `1.21.10`
- Fabric Loader: `0.16.9`
- Fabric API: `0.138.4+1.21.10`
- Yarn mappings: `1.21.10+build.3`
- Cloth Config: `15.0.140`
- Mod Menu: `11.0.3`

## Licencia

GPL-3.0-only. Ver archivo `LICENSE`.

