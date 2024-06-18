package com.musigma.controllers;

import com.musigma.controllers.workspaces.CalendarController;
import com.musigma.controllers.workspaces.HomeController;
import com.musigma.controllers.workspaces.StockController;
import com.musigma.controllers.workspaces.TicketController;
import com.musigma.models.Festival;
import com.musigma.models.exception.FestivalException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainController {

    private static final String APP_NAME = "Musigma";

    private static final String EXT_NAME = "*.mgm";

    private static final int MAX_RECENT_FILES = 10;

    private static final String STATE_FILEPATH = "previousSession.ser";

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

    private WorkspaceController.WorkspaceRegister currentWorkspace;
    
    private WorkspaceController currentWorkspaceController;

    @FXML
    private VBox pageMenu;

    @FXML
    private Menu recentFileMenu;

    @FXML
    private Pane workspace;

    @FXML
    public void initialize(Stage stage) {
        this.stage = stage;
        recentFiles = new ArrayList<>();
        stage.setOnCloseRequest(e -> saveState());
        for (WorkspaceController.WorkspaceRegister workspace: WORKSPACES)
            addWorkspace(workspace);
        loadState();
    }

    private void loadState() {
        File previousStateFile = new File(STATE_FILEPATH);
        if (previousStateFile.exists()) {
            try (
                FileInputStream fis = new FileInputStream(previousStateFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                ArrayList<File> previousRecentFiles = (ArrayList<File>) ois.readObject();
                if (previousRecentFiles != null && !previousRecentFiles.isEmpty())
                    recentFiles.addAll(
                            previousRecentFiles
                                    .stream()
                                    .filter(file -> file != null && file.exists())
                                    .collect(Collectors.toList())
                    );
                if (!recentFiles.isEmpty())
                    loadFestival(Festival.Festival(recentFiles.get(0)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else newFestival();
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
        // TODO: use this dialog only when festival really changed
        if (!askToSaveFestival())
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

    private void loadRecentFileMenu() {
        for (File file: recentFiles) {
            MenuItem menu = new MenuItem(file.getName());
            menu.setOnAction(e -> openFestival(file));
            recentFileMenu.getItems().add(menu);
        }
    }

    private boolean askToSaveFestival() {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                String.format("Le festival \"%s\" à été modifié, voulez vous le sauvegarder ?", this.festival.getName()),
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL
        );
        alert.showAndWait();
        ButtonType result = alert.getResult();
        if (result.equals(ButtonType.YES))
            saveFestival();
        else if (result.equals(ButtonType.CANCEL))
            return true;
        return false;
    }

    private void loadFestival(Festival festival) {
        // TODO: use this dialog only when festival really changed
        if (this.festival != null && askToSaveFestival())
            return;
        File file = festival.getFile();
        if (file != null)
            addRecentFile(file);
        try {
            this.festival = festival;
            loadWorkspace(DEFAULT_WORKSPACE);
        } catch (Exception e) {}
    }

    @FXML
    private void newFestival() {
        try {
            loadFestival(new Festival(
                "Nouveau festival",
                LocalDateTime.now(),
                0,
                1,
                "Quelque part sur Terre"
            ));
        } catch (Exception e) {}
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
            loadFestival(Festival.Festival(file));
        } catch (FestivalException e) {
            throw new RuntimeException(e);
        }
    }

    private void openFestival(File file) {
        try {
            loadFestival(Festival.Festival(file));
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

        register.openButton = pageButtonBox;
        pageMenu.getChildren().add(pageButtonBox);
        pageButtonBox.setOnMouseClicked(ev -> {
            if (festival == null || currentWorkspace == register)
                return;
            try {
                loadWorkspace(register);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        currentWorkspaceController = fxmlLoader.getController();
        currentWorkspaceController.initialize(festival);
        stage.setTitle(String.format("%s - %s", APP_NAME, register.name));

        if (currentWorkspace != null)
            currentWorkspace.openButton.getStyleClass().remove(CURRENT_WORKSPACE_STYLECLASS);
        currentWorkspace = register;
        register.openButton.getStyleClass().add(CURRENT_WORKSPACE_STYLECLASS);
    }
}