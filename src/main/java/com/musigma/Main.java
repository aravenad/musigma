package com.musigma;

import com.musigma.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class to launch the application
 */
public class Main extends Application {

    // Path to the main FXML view file
    private static final String VIEW_PATH = "/com/musigma/views/main-view.fxml";

    /**
     * Start the application and load the main view
     * @param stage the stage
     * @throws IOException if the FXML file is not found
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_PATH));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);

        MainController controller = fxmlLoader.getController();
        controller.initialize(stage);
    }

    /**
     * Main method to launch the application
     * @param args the arguments passed to the application at launch
     */
    public static void main(String[] args) {
        launch(args);
    }
}