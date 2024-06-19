package com.musigma.utils;

import com.musigma.utils.exceptionMethods.Runner;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.util.Arrays;
import java.util.logging.Logger;

public class Dialogs {

    // Logger pour la classe Dialogs
    private static final Logger LOGGER = Log.getLogger(Log.class);

    // Nom de fichier par défaut et extension pour le FileChooser
    private static final String FILENAME = "Musigma";
    private static final String EXT_NAME = "*.mgm";

    /**
     * Méthode pour exécuter une opération encapsulée dans un bloc try-catch.
     *
     * @param errorMsg message d'erreur à afficher en cas d'exception
     * @param function fonction (Runner) contenant le code à exécuter
     */
    public static void tryCatch(String errorMsg, Runner function) {
        try {
            function.run();
        } catch (Exception e) {
            // En cas d'exception, enregistrer les détails dans le journal (LOGGER)
            Arrays.stream(e.getStackTrace()).forEach(error -> LOGGER.severe(String.format("%s : %s", errorMsg, error)));

            // Afficher une boîte de dialogue d'erreur avec le message d'erreur
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    String.format("L'application a rencontré une erreur :\n%s :\n%s", errorMsg, e.getMessage())
            );
            alert.showAndWait();
        }
    }

    /**
     * Méthode pour exécuter une opération encapsulée dans un bloc try-catch avec un message de succès.
     *
     * @param errorMsg  message d'erreur à afficher en cas d'exception
     * @param succesMsg message de succès à afficher si l'opération réussit
     * @param function  fonction (Runner) contenant le code à exécuter
     */
    public static void tryCatch(String errorMsg, String succesMsg, Runner function) {
        try {
            function.run();

            // Afficher une boîte de dialogue d'information avec le message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION, succesMsg);
            alert.showAndWait();
        } catch (Exception e) {
            // En cas d'exception, enregistrer les détails dans le journal (LOGGER)
            Arrays.stream(e.getStackTrace()).forEach(error -> LOGGER.severe(String.format("%s : %s", errorMsg, error)));

            // Afficher une boîte de dialogue d'erreur avec le message d'erreur
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    String.format("L'application a rencontré une erreur :\n%s :\n%s", errorMsg, e.getMessage())
            );
            alert.showAndWait();
        }
    }

    /**
     * Méthode pour afficher un FileChooser avec un titre spécifié.
     *
     * @param title titre du FileChooser
     * @return FileChooser configuré avec le titre et l'extension de fichier par défaut
     */
    public static FileChooser askFile(String title) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(FILENAME + " Files", EXT_NAME));
        fc.setTitle(title);
        return fc;
    }

    /**
     * Méthode pour afficher un FileChooser avec un titre spécifié et un nom de fichier initial.
     *
     * @param title    titre du FileChooser
     * @param fileName nom de fichier initial à afficher dans le FileChooser
     * @return FileChooser configuré avec le titre, l'extension de fichier par défaut, et le nom de fichier initial
     */
    public static FileChooser askFile(String title, String fileName) {
        FileChooser fc = askFile(title);
        fc.setInitialFileName(fileName);
        return fc;
    }
}
