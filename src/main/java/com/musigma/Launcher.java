package com.musigma;

import javafx.application.Application;

/**
 * Point d'entrée du jar distribuable ({@code java -jar genevent.jar}).
 * <p>
 * Lancée directement, une classe qui étend {@link Application} exige que JavaFX soit chargé comme module ;
 * cette classe intermédiaire permet de démarrer l'application depuis un jar contenant toutes ses dépendances.
 */
public class Launcher {

    private Launcher() {
    }

    /**
     * Démarre l'application.
     *
     * @param args les arguments passés à l'application
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
