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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.musigma.utils.Dialogs.askFile;
import static com.musigma.utils.Dialogs.tryCatch;

public class MainController {

    private static final String APP_NAME = "Musigma";

    private static final int MAX_RECENT_FILES = 10;

    private static final String STATE_FILEPATH = "previousSession.ser";

    private static final WorkspaceController.WorkspaceRegister[] WORKSPACES = {
        HomeController.REGISTER,
        CalendarController.REGISTER,
        StockController.REGISTER,
        TicketController.REGISTER,
    };

    private static final WorkspaceController.WorkspaceRegister DEFAULT_WORKSPACE = HomeController.REGISTER;

    private static final String CURRENT_WORKSPACE_STYLECLASS = "currentWorkspace";

    private Festival festival;

    private int festivalHash;

    private ArrayList<File> recentFiles;

    private WorkspaceController.WorkspaceRegister currentWorkspace;
    
    private WorkspaceController currentWorkspaceController;

    private Stage stage;

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
            tryCatch(
                "Chargement de l'état précédent de l'application impossible",
                () -> {
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
                    }
            });
        } else newFestival();
    }

    private void saveState() {
            tryCatch(
        "Sauvegarde de l'état actuel de l'application impossible",
            () -> {
                try (
                    FileOutputStream fos = new FileOutputStream(STATE_FILEPATH);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                ) {
                    oos.writeObject(recentFiles);
                }
        });
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
        if (askToSaveFestival())
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
        if (festival.getFile() != null && festivalHash == festival.hashCode())
            return true;
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
            return false;
        return true;
    }

    private void loadFestival(Festival festival) {
        if (this.festival != null && !askToSaveFestival())
            return;
        File file = festival.getFile();
        if (file != null)
            addRecentFile(file);
        tryCatch(
            "Chargement du festival impossible",
            () -> {
                this.festival = festival;
                festivalHash = festival.hashCode();
                loadWorkspace(DEFAULT_WORKSPACE);
        });
    }

    @FXML
    private void newFestival() {
        tryCatch(
        "Création du nouveau festival impossible",
            () -> loadFestival(new Festival(
                "Nouveau festival",
                LocalDateTime.now(),
                0,
                1,
                "Quelque part sur Terre"
        )));
    }

    @FXML
    private void openFestival() {
        tryCatch(
    "Ouverture du fichier du festival impossible",
            () -> {
                File file = askFile("Open").showOpenDialog(stage);
                if (file == null)
                    return;
                loadFestival(Festival.Festival(file));
        });
    }

    private void openFestival(File file) {
        tryCatch(
    "Ouverture du fichier du festival impossible",
            () -> loadFestival(Festival.Festival(file))
        );
    }

    @FXML
    private void saveFestival() {
        if (festival.getFile() == null)
            saveFestivalAs();
        else
            tryCatch(
                "Sauvegarde du festival impossible",
                "Festival sauvegardé",
                () -> {
                    festival.save();
                    festivalHash = festival.hashCode();
                }
            );
    }

    @FXML
    private void saveFestivalAs() {
        File file = askFile("Enregistrer sous", festival.getName()).showSaveDialog(stage);
        if (file == null)
            return;
        tryCatch(
            "Sauvegarde du festival impossible",
            "Festival sauvegardé",
            () -> {
                festival.setFile(file);
                addRecentFile(file);
                festival.save();
                festivalHash = festival.hashCode();
        });
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
        pageLabel.setStyle("-fx-font-size: 8pt;");

        VBox pageButtonBox = new VBox();
        pageButtonBox.setAlignment(Pos.CENTER);
        pageButtonBox.getChildren().addAll(icon, pageLabel);
        pageButtonBox.setPadding(new Insets(8));

        register.openButton = pageButtonBox;
        pageMenu.getChildren().add(pageButtonBox);
        pageButtonBox.setOnMouseClicked(ev -> {
            if (festival == null || currentWorkspace == register)
                return;
            tryCatch(
                "Accès à l'espace de travail impossible",
                () -> loadWorkspace(register)
            );
        });
    }

    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException, FestivalException {
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