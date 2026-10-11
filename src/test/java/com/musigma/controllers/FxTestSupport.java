package com.musigma.controllers;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Démarrage de JavaFX pour les tests de l'interface.
 * Sans affichage (intégration continue), JavaFX n'est pas démarré et les tests concernés sont ignorés.
 */
public final class FxTestSupport {

    private static Boolean available;

    private FxTestSupport() {
    }

    /**
     * Indique si un affichage est disponible : sous Linux, JavaFX attend indéfiniment s'il n'y en a pas.
     *
     * @return true si un affichage est disponible
     */
    private static boolean hasDisplay() {
        if (Boolean.getBoolean("java.awt.headless"))
            return false;
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux"))
            return true;
        return System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
    }

    /**
     * Démarre JavaFX si ce n'est pas déjà fait et si un affichage est disponible.
     *
     * @return true si JavaFX est utilisable
     */
    public static synchronized boolean start() {
        if (available == null) {
            if (!hasDisplay()) {
                available = false;
            } else {
                try {
                    Platform.startup(() -> {
                    });
                    available = true;
                } catch (IllegalStateException alreadyStarted) {
                    available = true;
                } catch (Throwable noToolkit) {
                    available = false;
                }
            }
        }
        return available;
    }

    /**
     * Exécute une action sur le thread JavaFX et renvoie son résultat, ou ignore le test sans affichage.
     *
     * @param action l'action à exécuter
     * @param <T>    le type du résultat
     * @return le résultat de l'action
     * @throws Exception si l'action échoue ou dépasse 10 secondes
     */
    public static <T> T onFxThread(Callable<T> action) throws Exception {
        assumeTrue(start(), "JavaFX ne peut pas démarrer sans affichage");
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
