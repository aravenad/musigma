package com.musigma.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Préférences de l'utilisateur (AUDIT.md A5), stockées ici dans un nœud temporaire.
 */
@DisplayName("Préférences")
class SettingsTest {

    Preferences node;
    Settings settings;

    @BeforeEach
    void setUp() {
        node = Preferences.userRoot().node("musigma-test-" + UUID.randomUUID());
        settings = new Settings(node);
    }

    @AfterEach
    void tearDown() throws Exception {
        node.removeNode();
        Preferences.userRoot().flush();
    }

    @Test
    @DisplayName("Les fichiers récents sont conservés dans l'ordre, sans les fichiers disparus")
    void recentFiles(@TempDir Path dir) throws Exception {
        File a = Files.createFile(dir.resolve("a.mgm")).toFile();
        File b = Files.createFile(dir.resolve("b.mgm")).toFile();
        File missing = dir.resolve("absent.mgm").toFile();
        settings.setRecentFiles(List.of(b, missing, a), 10);
        assertEquals(List.of(b, a), settings.getRecentFiles(10));
        settings.setRecentFiles(List.of(a), 10);
        assertEquals(List.of(a), settings.getRecentFiles(10), "Les anciennes entrées sont effacées");
    }

    @Test
    @DisplayName("Le zoom reste dans ses bornes")
    void zoom() {
        assertEquals(100, settings.getZoom());
        assertEquals(Settings.MAX_ZOOM, settings.setZoom(1000));
        assertEquals(Settings.MIN_ZOOM, settings.setZoom(1));
        assertEquals(Settings.MIN_ZOOM, settings.getZoom());
    }

    @Test
    @DisplayName("Le thème et l'aide contextuelle sont mémorisés")
    void themeAndHelp() {
        assertFalse(settings.isDarkTheme());
        assertTrue(settings.isHelpVisible());
        settings.setDarkTheme(true);
        settings.setHelpVisible(false);
        Settings reloaded = new Settings(node);
        assertTrue(reloaded.isDarkTheme());
        assertFalse(reloaded.isHelpVisible());
    }
}
