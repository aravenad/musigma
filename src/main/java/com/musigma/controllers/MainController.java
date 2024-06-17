package com.musigma.controllers;

import com.musigma.models.Festival;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    private Festival festival;

    @FXML
    Button minimizeButton, closeButton;

    @FXML
    VBox pageMenu;

    @FXML
    Pane workspace;

    @FXML
    public void initialize() {
        minimizeButton.setOnAction(this::handleButtonAction);
        closeButton.setOnAction(this::handleButtonAction);

        addHoverEffect(minimizeButton, "darkgrey");
        addHoverEffect(closeButton, "crimson");
    }

    private void handleButtonAction(ActionEvent event) {
        Button button = (Button) event.getSource();
        Stage window = (Stage) button.getScene().getWindow();
        switch (button.getId()) {
            case "minimizeButton":
                window.setIconified(true);
                System.out.println("Minimize button clicked");
                break;
            case "closeButton":
                window.close();
                System.out.println("Close button clicked");
                break;
        }
    }

    private void addHoverEffect(Button button, String color) {
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + color + ";"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent;"));
    }

    public void addWorkspace(WorkspaceController.WorkspaceRegister register) {
        ImageView icon = new ImageView();
        icon.setImage(new Image(getClass().getResourceAsStream(register.iconPath)));
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        icon.setPreserveRatio(true);
        icon.setPickOnBounds(true);

        Label pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: #f3f3f5;");
        pageLabel.setText(register.name);
        VBox pageButtonBox = new VBox();

        pageButtonBox.setAlignment(Pos.CENTER);
        pageButtonBox.getChildren().addAll(icon, pageLabel);
        pageButtonBox.setOnMouseClicked(ev -> {
            try {
                setWorkspace(register);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pageMenu.getChildren().add(pageButtonBox);
    }

    public void setWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        ((WorkspaceController) fxmlLoader.getController()).setFestival(festival);
    }
}