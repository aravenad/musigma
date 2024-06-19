package com.musigma.utils;

import com.musigma.utils.exceptionMethods.Runner;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.util.Arrays;
import java.util.logging.Logger;


public class Dialogs {

    private static final Logger LOGGER = Log.getLogger(Log.class);

    private static final String FILENAME = "Musigma";

    private static final String EXT_NAME = "*.mgm";

    public static void tryCatch(String errorMsg, Runner function) {
        try {
            function.run();
        } catch (Exception e) {
            for (StackTraceElement error : e.getStackTrace())
                LOGGER.severe(String.format("%s : %s", errorMsg, error));
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                String.format("L'application a rencontré une erreur :\n%s :\n%s", errorMsg, e.getMessage())
            );
            alert.showAndWait();
        }
    }

    public static void tryCatch(String errorMsg, String succesMsg, Runner function) {
        try {
            function.run();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, succesMsg);
            alert.showAndWait();
        } catch (Exception e) {
            for (StackTraceElement error : e.getStackTrace())
                LOGGER.severe(String.format("%s : %s", errorMsg, error));
            Alert alert = new Alert(
                Alert.AlertType.ERROR,
                String.format("L'application a rencontré une erreur :\n%s :\n%s", errorMsg, e.getMessage())
            );
            alert.showAndWait();
        }
    }

    public static FileChooser askFile(String title) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(FILENAME + " Files", EXT_NAME));
        fc.setTitle(title);
        return fc;
    }

    public static FileChooser askFile(String title, String fileName) {
        FileChooser fc = askFile(title);
        fc.setInitialFileName(fileName);
        return fc;
    }
}
