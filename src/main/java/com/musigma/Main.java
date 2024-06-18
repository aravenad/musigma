package com.musigma;

import com.musigma.controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.rmi.RemoteException;

import atlantafx.base.theme.*;

public class Main extends Application {

    private static final String VIEW_PATH = "/com/musigma/views/main-view.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_PATH));
        Scene scene = new Scene(fxmlLoader.load());
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        stage.show();
        stage.setScene(scene);

        MainController controller = fxmlLoader.getController();
        controller.initialize(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}