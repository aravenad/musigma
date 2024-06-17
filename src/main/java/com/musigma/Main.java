package com.musigma;

import com.musigma.controllers.CalendarController;
import com.musigma.controllers.MainController;
import com.musigma.controllers.StockController;
import com.musigma.controllers.TicketController;
import com.musigma.controllers.WorkspaceController.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static final WorkspaceRegister[] WORKSPACE_REGISTERS = {
        CalendarController.REGISTER,
        TicketController.REGISTER,
        StockController.REGISTER
    };

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/musigma/views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();

        MainController controller = fxmlLoader.getController();
        for (WorkspaceRegister register: WORKSPACE_REGISTERS) {
            controller.addWorkspace(register);
        }
        controller.setWorkspace(WORKSPACE_REGISTERS[0]);
    }

    public static void main(String[] args) {
        launch(args);
    }
}