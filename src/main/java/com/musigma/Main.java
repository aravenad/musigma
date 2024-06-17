package com.musigma;

import com.musigma.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/musigma/views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();

        MainController controller = fxmlLoader.getController();

//        controller.addWorkspace(
//            "test",
//            Objects.requireNonNull(getClass().getResource("/com/musigma/images/home.png")).getPath(),
//            Objects.requireNonNull(getClass().getResource("/com/musigma/views/test.fxml")).getPath()
//        );

//        System.out.println("/com/musigma/views/test.fxml");
//        controller.setWorkspace("/com/musigma/views/test.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}