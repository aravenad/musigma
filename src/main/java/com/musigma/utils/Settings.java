package com.musigma.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Préférences de l'utilisateur conservées d'une session à l'autre :
 * fichiers récents, thème, zoom et affichage de l'aide contextuelle.
 * Elles sont enregistrées dans le profil de l'utilisateur, et non dans le dossier courant.
 */
public class Settings {

    private static final Logger LOGGER = Log.getLogger(Settings.class);

    private static final String RECENT_FILE_KEY = "recentFile.";
    private static final String DARK_THEME_KEY = "darkTheme";
    private static final String ZOOM_KEY = "zoom";
    private static final String HELP_VISIBLE_KEY = "helpVisible";

    /**
     * Zoom minimal, maximal et pas de zoom, en pourcentage.
     */
    public static final int MIN_ZOOM = 70, MAX_ZOOM = 160, ZOOM_STEP = 10;

    private final Preferences preferences;

    /**
     * Crée les préférences stockées dans le nœud donné.
     *
     * @param preferences le nœud de préférences
     */
    public Settings(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Retourne les préférences de l'utilisateur pour Musigma.
     *
     * @return les préférences de l'utilisateur
     */
    public static Settings user() {
        return new Settings(Preferences.userRoot().node("com/musigma"));
    }

    /**
     * Retourne les fichiers récents qui existent encore, du plus récent au plus ancien.
     *
     * @param max le nombre maximal de fichiers
     * @return les fichiers récents
     */
    public List<File> getRecentFiles(int max) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            String path = preferences.get(RECENT_FILE_KEY + i, null);
            if (path != null && new File(path).exists())
                files.add(new File(path));
        }
        return files;
    }

    /**
     * Enregistre les fichiers récents.
     *
     * @param files les fichiers, du plus récent au plus ancien
     * @param max   le nombre maximal de fichiers conservés
     */
    public void setRecentFiles(List<File> files, int max) {
        for (int i = 0; i < max; i++) {
            if (i < files.size())
                preferences.put(RECENT_FILE_KEY + i, files.get(i).getAbsolutePath());
            else
                preferences.remove(RECENT_FILE_KEY + i);
        }
        flush();
    }

    /**
     * Indique si le thème sombre est choisi.
     *
     * @return true pour le thème sombre
     */
    public boolean isDarkTheme() {
        return preferences.getBoolean(DARK_THEME_KEY, false);
    }

    /**
     * Choisit le thème sombre ou clair.
     *
     * @param darkTheme true pour le thème sombre
     */
    public void setDarkTheme(boolean darkTheme) {
        preferences.putBoolean(DARK_THEME_KEY, darkTheme);
        flush();
    }

    /**
     * Retourne le zoom de l'interface, en pourcentage.
     *
     * @return le zoom, entre {@link #MIN_ZOOM} et {@link #MAX_ZOOM}
     */
    public int getZoom() {
        return clampZoom(preferences.getInt(ZOOM_KEY, 100));
    }

    /**
     * Définit le zoom de l'interface, ramené entre {@link #MIN_ZOOM} et {@link #MAX_ZOOM}.
     *
     * @param zoom le zoom en pourcentage
     * @return le zoom retenu
     */
    public int setZoom(int zoom) {
        int clamped = clampZoom(zoom);
        preferences.putInt(ZOOM_KEY, clamped);
        flush();
        return clamped;
    }

    private static int clampZoom(int zoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }

    /**
     * Indique si la zone d'aide contextuelle est affichée.
     *
     * @return true si l'aide est affichée
     */
    public boolean isHelpVisible() {
        return preferences.getBoolean(HELP_VISIBLE_KEY, true);
    }

    /**
     * Affiche ou masque la zone d'aide contextuelle.
     *
     * @param visible true pour afficher l'aide
     */
    public void setHelpVisible(boolean visible) {
        preferences.putBoolean(HELP_VISIBLE_KEY, visible);
        flush();
    }

    private void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LOGGER.warning("Les préférences n'ont pas pu être enregistrées : " + e.getMessage());
        }
    }
}
