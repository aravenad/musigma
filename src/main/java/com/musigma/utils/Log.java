package com.musigma.utils;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Classe utilitaire pour la gestion des logs.
 */
public class Log {
    /**
     * Logger pour afficher les logs.
     */
    private static final Logger LOGGER = getLogger(Log.class);

    /**
     * Gestionnaire de logs.
     */
    private static final LogManager logManager = LogManager.getLogManager();

    static {
        try {
            logManager.readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
        } catch (IOException e) {
            LOGGER.severe("Cannot read configuration file");
        }
    }

    /**
     * Retourne un Logger pour une classe donnée.
     *
     * @param klass la classe pour laquelle un Logger est requis
     * @param <T>   le type de la classe
     * @return un Logger pour la classe spécifiée
     */
    public static <T> Logger getLogger(Class<T> klass) {
        return Logger.getLogger(klass.getName());
    }
}
