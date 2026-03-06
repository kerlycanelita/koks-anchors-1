package com.ppuetate.koksanchors.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ppuetate.koksanchors.KoKsAnchors;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestor de configuracion persistente.
 *
 * Carga/guarda AnchorSettings como JSON en el directorio de config de Fabric.
 * Thread-safe mediante synchronized.
 */
public final class AnchorConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve(KoKsAnchors.MOD_ID + ".json");
    private static final Path LEGACY_CONFIG_PATH = CONFIG_DIR.resolve("koksanchors.json");

    private static AnchorSettings current = new AnchorSettings();

    private AnchorConfigManager() {}

    public static synchronized void load() {
        AnchorSettings loaded = null;
        Path loadPath = resolveLoadPath();
        boolean usingLegacyPath = LEGACY_CONFIG_PATH.equals(loadPath);

        if (Files.isRegularFile(loadPath)) {
            try (Reader reader = Files.newBufferedReader(loadPath)) {
                loaded = GSON.fromJson(reader, AnchorSettings.class);
            } catch (Exception ex) {
                KoKsAnchors.LOGGER.error("[KoKs Anchors] No se pudo leer config, usando defaults", ex);
            }
        }

        if (loaded == null) {
            loaded = new AnchorSettings();
        }

        loaded.sanitize();
        current = loaded;
        boolean saved = saveCurrent();
        if (saved && usingLegacyPath) {
            removeLegacyConfigFile();
        }
    }

    public static synchronized AnchorSettings snapshot() {
        return current.copy();
    }

    public static synchronized AnchorSettings current() {
        return current;
    }

    public static synchronized void updateAndSave(AnchorSettings settings) {
        AnchorSettings newSettings = settings == null ? new AnchorSettings() : settings.copy();
        newSettings.sanitize();
        current = newSettings;
        saveCurrent();
    }

    private static Path resolveLoadPath() {
        if (Files.isRegularFile(CONFIG_PATH)) {
            return CONFIG_PATH;
        }
        if (Files.isRegularFile(LEGACY_CONFIG_PATH)) {
            KoKsAnchors.LOGGER.info(
                "[KoKs Anchors] Config legado detectada, migrando a {}",
                CONFIG_PATH.getFileName()
            );
            return LEGACY_CONFIG_PATH;
        }
        return CONFIG_PATH;
    }

    private static boolean saveCurrent() {
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(current, writer);
            }
            return true;
        } catch (IOException ex) {
            KoKsAnchors.LOGGER.error("[KoKs Anchors] No se pudo guardar config", ex);
            return false;
        }
    }

    private static void removeLegacyConfigFile() {
        try {
            if (Files.deleteIfExists(LEGACY_CONFIG_PATH)) {
                KoKsAnchors.LOGGER.info(
                    "[KoKs Anchors] Config legado eliminada: {}",
                    LEGACY_CONFIG_PATH.getFileName()
                );
            }
        } catch (IOException ex) {
            KoKsAnchors.LOGGER.warn(
                "[KoKs Anchors] No se pudo eliminar config legado {}",
                LEGACY_CONFIG_PATH,
                ex
            );
        }
    }
}
