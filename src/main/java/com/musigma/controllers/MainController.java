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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class MainController {

    private static final String APP_NAME = "Musigma";

    private static final String EXT_NAME = "*.mgm";

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

    private ArrayList<File> recentFiles;

    private Stage stage;

    private Node currentWorkspaceButton;
    
    private WorkspaceController currentWorkspaceController;

    @FXML
    private VBox pageMenu;

    @FXML
    private Menu recentFileMenu;

    @FXML
    private Pane workspace;

    @FXML
    public void initialize(Stage stage) throws IOException {
        this.stage = stage;
        loadState();
        stage.setOnHiding(e -> saveState());
        for (WorkspaceController.WorkspaceRegister workspace: WORKSPACES)
            addWorkspace(workspace);
        loadRecentFileMenu();
        loadWorkspace(DEFAULT_WORKSPACE);
    }

    private void loadRecentFileMenu() {
        for (File file: recentFiles) {
            MenuItem menu = new MenuItem(file.getName());
            menu.setOnAction(e -> openFestival(file));
            recentFileMenu.getItems().add(menu);
        }
    }

    private void loadState() {
        File previousStateFile = new File(STATE_FILEPATH);
        if (previousStateFile.exists()) {
            try (
                FileInputStream fis = new FileInputStream(previousStateFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                ArrayList<File> previousRecentFiles = (ArrayList<File>) ois.readObject();
                recentFiles = new ArrayList<>();
                if (previousRecentFiles != null && !previousRecentFiles.isEmpty())
                    recentFiles.addAll(previousRecentFiles.stream().filter(file -> file != null && file.exists()).collect(Collectors.toList()));
                festival = !recentFiles.isEmpty() ? Festival.Festival(recentFiles.get(0)) : new Festival(
                    "Nouveau festival",
                    LocalDateTime.now(),
                    0,
                    1,
                    "Quelque part sur Terre"
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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

    private void addRecentFile(File file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        if (recentFiles.size() > MAX_RECENT_FILES)
            recentFiles.remove(MAX_RECENT_FILES);
        recentFileMenu.getItems().clear();
        loadRecentFileMenu();
    }

    @FXML
    private void openFestival() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(APP_NAME + " Files", EXT_NAME));
            fc.setTitle("Open");
            File file = fc.showOpenDialog(stage);
            if (file == null)
                return;
            festival = Festival.Festival(file);
            addRecentFile(file);
            currentWorkspaceController.initialize(festival);
        } catch (FestivalException e) {
            throw new RuntimeException(e);
        }
    }

    private void openFestival(File file) {
        try {
            festival = Festival.Festival(file);
            addRecentFile(file);
            currentWorkspaceController.initialize(festival);
        } catch (FestivalException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void saveFestival() {
        if (festival.getFile() == null) {
            saveFestivalAs();
        } else {
            try {
                festival.save();
            } catch (FestivalException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void saveFestivalAs() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(APP_NAME + " Files", EXT_NAME));
        fc.setInitialFileName(festival.getName());
        fc.setTitle("Save as");
        File file = fc.showSaveDialog(stage);
        if (file == null)
            return;
        try {
            festival.setFile(file);
            addRecentFile(file);
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
                if (currentWorkspaceButton != null)
                    currentWorkspaceButton.getStyleClass().remove(CURRENT_WORKSPACE_STYLECLASS);
                pageButtonBox.getStyleClass().add(CURRENT_WORKSPACE_STYLECLASS);
                currentWorkspaceButton = pageButtonBox;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pageMenu.getChildren().add(pageButtonBox);
    }

    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        currentWorkspaceController = fxmlLoader.getController();
        currentWorkspaceController.initialize(festival);
        stage.setTitle(String.format("%s - %s", APP_NAME, register.name));
    }
}