package com.musigma.controllers;

import com.musigma.controllers.workspaces.CalendarController;
import com.musigma.controllers.workspaces.HomeController;
import com.musigma.controllers.workspaces.StockController;
import com.musigma.controllers.workspaces.TicketController;
import com.musigma.models.Festival;
import com.musigma.models.exception.FestivalException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;

public class MainController {

    private static final String APP_NAME = "Musigma";

    private static final int MAX_RECENT_FILES = 10;

    private static final String STATE_FILEPATH = "old_state.ser";

    private static final WorkspaceController.WorkspaceRegister[] WORKSPACES = {
            HomeController.REGISTER,
            CalendarController.REGISTER,
            TicketController.REGISTER,
            StockController.REGISTER
    };

    private static final WorkspaceController.WorkspaceRegister DEFAULT_WORKSPACE = HomeController.REGISTER;

    private static final String CURRENT_WORKSPACE_STYLECLASS = "currentWorkspace";

    private Festival festival;

    private File[] recentFiles;

    private Stage stage;

    private Node activateWorkspaceButton;

    @FXML
    private VBox pageMenu;

    @FXML
    private Pane workspace;

    @FXML
    public void initialize(Stage stage) throws IOException {
        this.stage = stage;
        loadState();
        stage.setOnHiding(e -> saveState());
        for (WorkspaceController.WorkspaceRegister workspace: WORKSPACES)
            addWorkspace(workspace);
        loadWorkspace(DEFAULT_WORKSPACE);
    }

    private void loadState() {
        File previousStateFile = new File(STATE_FILEPATH);
        if (previousStateFile.exists()) {
            try (
                FileInputStream fis = new FileInputStream(previousStateFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                recentFiles = (File[]) ois.readObject();
                if (recentFiles != null && recentFiles.length > 0) {
                    recentFiles = Arrays.stream(recentFiles).filter(file -> file != null && file.exists()).toArray(File[]::new);
                }
                festival = recentFiles.length > 0 ? Festival.Festival(recentFiles[0]) : new Festival(
                    "Nouveau festival",
                    LocalDateTime.now().plusDays(1),
                    0,
                    1,
                    "Somewhere on Earth"
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        recentFiles = new File[MAX_RECENT_FILES];
    }

    private void saveState() {
        try (
            FileOutputStream fos = new FileOutputStream(STATE_FILEPATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(recentFiles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFestival() {

    }

    @FXML
    private void dragWindow(MouseEvent pressEvent) {
        Node node = (Node) pressEvent.getSource();
        node.setOnMouseDragged(dragEvent -> {
            stage.setX(dragEvent.getScreenX() - pressEvent.getSceneX());
            stage.setY(dragEvent.getScreenY() - pressEvent.getSceneY());
        });
    };

    @FXML
    private void closeWindow() {
        stage.close();
    }

    @FXML
    private void minimizeWindow() {
        stage.setIconified(true);
    }

    private String getFile(String title, int mode) {
        FileDialog fd = new FileDialog(new Frame(), title, mode);
        fd.setFilenameFilter((dir, name) -> name.endsWith(".mgm"));
        fd.setVisible(true);
        return fd.getFile();
    }

    private void addRecentFile(File file) {
        File[] newRecentFiles = new File[MAX_RECENT_FILES];
        newRecentFiles[0] = file;
        System.arraycopy(recentFiles, 0, newRecentFiles, 1, MAX_RECENT_FILES);
    }

    @FXML
    private void openFestival() {
        try {
            String filepath = getFile("Open festival", FileDialog.LOAD);
            if (filepath == null)
                return;
            File file = new File(filepath);
            festival = Festival.Festival(file);
            addRecentFile(file);
        } catch (FestivalException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void saveFestival() {
        try {
            if (festival.getFile() == null) {
                String filepath = getFile("Save as", FileDialog.SAVE);
                if (filepath == null)
                    return;
                File file = new File(filepath);
                festival.setFile(file);
                addRecentFile(file);
            }
            festival.save();
        } catch (FestivalException e) {
            throw new RuntimeException(e);
        }
    }

    public void addWorkspace(WorkspaceController.WorkspaceRegister register) {
        ImageView icon = new ImageView();
        icon.setImage(new Image(getClass().getResourceAsStream(register.iconPath)));
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        icon.setPreserveRatio(true);
        icon.setPickOnBounds(true);

        Label pageLabel = new Label();
        pageLabel.setText(register.name);

        VBox pageButtonBox = new VBox();
        pageButtonBox.setAlignment(Pos.CENTER);
        pageButtonBox.getChildren().addAll(icon, pageLabel);
        pageButtonBox.setPadding(new Insets(8));

        pageButtonBox.setOnMouseClicked(ev -> {
            if (festival == null)
                return;
            try {
                loadWorkspace(register);
                if (activateWorkspaceButton != null)
                    activateWorkspaceButton.getStyleClass().remove(CURRENT_WORKSPACE_STYLECLASS);
                pageButtonBox.getStyleClass().add(CURRENT_WORKSPACE_STYLECLASS);
                activateWorkspaceButton = pageButtonBox;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pageMenu.getChildren().add(pageButtonBox);
    }

    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        ((WorkspaceController) fxmlLoader.getController()).initialize(festival);
        stage.setTitle(String.format("%s - %s", APP_NAME, register.name));
    }
}