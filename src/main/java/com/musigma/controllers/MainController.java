package com.musigma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    Button minimizeButton, closeButton;

    @FXML
    VBox pageMenu;

    @FXML
    VBox workspace;

    @FXML
    public void initialize() {
        minimizeButton.setOnAction(this::handleButtonAction);
        closeButton.setOnAction(this::handleButtonAction);

        addHoverEffect(minimizeButton, "lightgrey");
        addHoverEffect(closeButton, "red");
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

    public void addWorkspace(String pageName, String pathToIcon, String pathToFXML) {
        System.out.println(pageName);
        System.out.println(pathToIcon);
        System.out.println(pathToFXML);
        VBox pageButtonBox = new VBox();
        pageButtonBox.setAlignment(Pos.CENTER);
//        ImageView icon = new ImageView();
//        icon.setImage(new Image(pathToIcon));
        Label pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: #f3f3f5;");
        pageLabel.setText(pageName);
        pageButtonBox.getChildren().addAll(pageLabel);
        pageButtonBox.setOnMouseClicked(ev -> {
            try {
                setWorkspace(pathToFXML);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pageMenu.getChildren().add(pageButtonBox);
    }

    public void setWorkspace(String pathToFXML) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource(pathToFXML));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
    }
}