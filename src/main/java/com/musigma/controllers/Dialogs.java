package com.musigma.controllers;

import com.musigma.controllers.components.CustomValidField;
import com.musigma.utils.Log;
import com.musigma.utils.exceptionMethods.Runner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import static com.musigma.controllers.MainController.ICON_PATH;

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
            newError(errorMsg + " :\n" + e.getMessage());
            Arrays.stream(e.getStackTrace()).forEach(error -> LOGGER.severe(String.format("%s : %s", errorMsg, error)));
        }
    }

    public static void newError(String errorMsg) {
        LOGGER.severe(errorMsg);
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                String.format("L'application a rencontré une erreur :\n%s", errorMsg)
        );
        ((Stage) alert.getDialogPane().getScene().getWindow())
                .getIcons()
                .add(new Image(Objects.requireNonNull(Dialogs.class.getResourceAsStream(ICON_PATH))));
        alert.showAndWait();
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION, succesMsg);
            alert.showAndWait();
            ((Stage) alert.getDialogPane().getScene().getWindow())
                    .getIcons()
                    .add(new Image(Objects.requireNonNull(Dialogs.class.getResourceAsStream(ICON_PATH))));
        } catch (Exception e) {
            newError(String.format("%s : %s", errorMsg, e.getMessage()));
            Arrays.stream(e.getStackTrace()).forEach(error -> LOGGER.severe(String.format("%s : %s", errorMsg, error)));
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


    public static void askValidForm(String title, String errorMsg, CustomValidField[] customValidFields, Runner nextCallback) {
        Stage subWindow = new Stage();
        subWindow.setTitle(title);
        subWindow.getIcons().add(new Image(Objects.requireNonNull(Dialogs.class.getResourceAsStream(ICON_PATH))));

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));

        Scene scene = new Scene(new Pane(grid));
        subWindow.setScene(scene);

        for (CustomValidField customValidField: customValidFields) {
            int rowIdx = grid.getRowCount();
            grid.add(customValidField.label, 0, rowIdx);
            grid.add(customValidField.node, 1, rowIdx);
            customValidField.node.setMaxWidth(1.7976931348623157E308);
        }

        Button cancelButton = new Button("Cancel"),
                validateButton = new Button("Validate");
        validateButton.getStyleClass().add("accent");

        HBox buttonsRow = new HBox(validateButton, cancelButton);
        buttonsRow.setAlignment(Pos.CENTER_RIGHT);
        buttonsRow.setSpacing(8);
        buttonsRow.setPadding(new Insets(8, 0, 0, 0));
        grid.add(buttonsRow, 0, grid.getRowCount(), 2, 1);

        cancelButton.setOnAction(e -> subWindow.close());
        validateButton.setOnAction(e -> {
            boolean valid = true;
            for (CustomValidField customValidField : customValidFields) {
                if (!customValidField.isValid()) {
                    customValidField.node.requestFocus();
                    valid = false;
                }
            }
            if (valid) {
                tryCatch(
                        errorMsg,
                        () -> {
                            nextCallback.run();
                            subWindow.close();
                        }
                );
            }
        });

        subWindow.show();
    }
}
