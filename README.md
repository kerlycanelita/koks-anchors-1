# KoKs Anchors

A Fabric mod for Minecraft **1.21.10** focused on **Respawn Anchor** interaction optimization for PvP (Anchor/Crystal), reducing desync and interaction spam.

## Project Status

KoKs Anchors is a **discontinued beta** and **will no longer receive updates**.

This repository is kept for historical reference only. The same features from this beta have been integrated into **KoHs Mods Suite**, which is the finalized and recommended version.

If you need support, improvements, or new releases, use KoHs Mods Suite instead of this project.

## What This Project Does

- Applies **client-side debounce** for Respawn Anchor placement.
- Applies **client-side debounce** for Glowstone charging interactions.
- Applies **server-side validation** to reject excessively fast interaction spam.
- Synchronizes the real anchor charge state via S2C payloads `anchor_confirm` and `anchor_reject`.
- Includes a config screen through **Mod Menu + Cloth Config** (when available).
- Includes a vanilla fallback config screen if Cloth Config is not installed.

## Technical Architecture

- Common entrypoint: `com.ppuetate.koksanchors.KoKsAnchors`
- Client entrypoint: `com.ppuetate.koksanchors.KoKsAnchorsClient`
- Server mixins: `ServerAnchorPlacementMixin` and `ServerAnchorInteractionMixin`
- Client mixins: `ClientBlockPlacementMixin` and `ClientAnchorRenderMixin`
- Networking: `AnchorNetworking` (payload IDs + S2C send)
- Client networking: `AnchorClientNetworking` (applies confirm/reject)
- Persistent config: `AnchorConfigManager` in `config/koks-anchors.json`

## Available Configuration

- `placementDebounceMs` (default: `50`, range: `0-500`)
- `chargeDebounceMs` (default: `50`, range: `0-500`)
- `antiGhostEnabled` (default: `true`)
- `fastRenderUpdate` (default: `true`)
- `suppressNeighborUpdates` (default: `true`)

## Requirements

- JDK **21**
- Windows, Linux, or macOS
- Internet connection (first run, to download Gradle dependencies)

## Build

### Windows (PowerShell / CMD)

```bat
.\gradlew.bat clean build
```

### Linux / macOS

```bash
./gradlew clean build
```

Generated artifacts:

- `build/libs/koks-anchors-<version>.jar`
- `build/libs/koks-anchors-<version>-sources.jar`

## Development Run

```bash
./gradlew runClient
./gradlew runServer
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Project Versions

- Minecraft: `1.21.10`
- Fabric Loader: `0.16.9`
- Fabric API: `0.138.4+1.21.10`
- Yarn mappings: `1.21.10+build.3`
- Cloth Config: `15.0.140`
- Mod Menu: `11.0.3`

## License

GPL-3.0-only. See `LICENSE`.

