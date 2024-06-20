package com.musigma.controllers;

import atlantafx.base.theme.CupertinoLight;
import com.musigma.controllers.workspaces.*;
import com.musigma.models.Festival;
import com.musigma.utils.Log;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.musigma.controllers.Dialogs.askFile;
import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Le contrôleur principal de l'application Musigma, gère l'initialisation,
 * la navigation entre les espaces de travail, et la gestion des fichiers récents.
 */
public class MainController {

    /**
     * Logger pour les messages de log.
     */
    private static final Logger LOGGER = Log.getLogger(Log.class);

    /**
     * Nom de l'application.
     */
    private static final String APP_NAME = "Musigma";
    /**
     * Chemin de l'icône de l'application.
     */
    protected static final String ICON_PATH = "/com/musigma/images/logo_full.png";

    /**
     * Nombre maximum de fichiers récents à conserver.
     */
    private static final int MAX_RECENT_FILES = 10;
    /**
     * Chemin du fichier d'état de la session précédente.
     */
    private static final String STATE_FILEPATH = "previousSession.ser";

    private static final WorkspaceController.WorkspaceRegister[] WORKSPACES = {
        HomeController.REGISTER,
        CalendarController.REGISTER,
        StockController.REGISTER,
        TicketController.REGISTER,
        AccountingController.REGISTER,
    }; // Tableau des espaces de travail disponibles

    private static final WorkspaceController.WorkspaceRegister DEFAULT_WORKSPACE = HomeController.REGISTER; // Espace de travail par défaut

    private static final String CURRENT_WORKSPACE_STYLECLASS = "currentWorkspace"; // Classe CSS pour l'espace de travail actuel

    private Festival festival; // Le festival actuel
    private int festivalHash; // Hash du festival pour détecter les modifications
    private ArrayList<File> recentFiles; // Liste des fichiers récents
    private WorkspaceController.WorkspaceRegister currentWorkspace; // Espace de travail actuel
    private WorkspaceController currentWorkspaceController; // Contrôleur de l'espace de travail actuel
    private Stage stage; // La fenêtre principale

    @FXML
    private VBox pageMenu; // Menu de la page
    @FXML
    private Menu recentFileMenu; // Menu des fichiers récents
    @FXML
    private Pane workspace; // Espace de travail

    /**
     * Initialise le contrôleur principal avec la fenêtre spécifiée.
     *
     * @param stage La fenêtre principale
     */
    @FXML
    public void initialize(Stage stage) {
        this.stage = stage;
        recentFiles = new ArrayList<>();
        stage.setOnHidden(e -> saveState());
        for (WorkspaceController.WorkspaceRegister workspace : WORKSPACES)
            addWorkspace(workspace);
        loadState();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(ICON_PATH))));
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        stage.show();
        LOGGER.info("Loaded main controller");
    }

    /**
     * Charge l'état de la session précédente depuis le fichier d'état.
     */
    private void loadState() {
        LOGGER.info("Loading previous state");
        File previousStateFile = new File(STATE_FILEPATH);
        if (previousStateFile.exists()) {
            try (
                    FileInputStream fis = new FileInputStream(previousStateFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
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
                else newFestival();
            } catch (Exception e) {
                newFestival();
            }
        } else newFestival();
        LOGGER.info("Loaded previous state");
    }

    /**
     * Sauvegarde l'état actuel de l'application dans le fichier d'état.
     */
    private void saveState() {
        LOGGER.info("Saving current state");
        tryCatch(
    "Sauvegarde de l'état actuel de l'application impossible",
            () -> {
                try (
                    FileOutputStream fos = new FileOutputStream(STATE_FILEPATH);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)
                ) {
                    oos.writeObject(recentFiles);
                }
            });
        LOGGER.info("Saved current state");
    }

    /**
     * Gère le glissement de la fenêtre lorsqu'on la déplace avec la souris.
     *
     * @param pressEvent Événement de pression de la souris
     */
    @FXML
    private void dragWindow(MouseEvent pressEvent) {
        Node node = (Node) pressEvent.getSource();
        node.setOnMouseDragged(dragEvent -> {
            stage.setX(dragEvent.getScreenX() - pressEvent.getSceneX());
            stage.setY(dragEvent.getScreenY() - pressEvent.getSceneY());
        });
        LOGGER.info("Dragged window");
    }

    /**
     * Ferme la fenêtre après avoir demandé si l'utilisateur souhaite sauvegarder le festival.
     */
    @FXML
    private void closeWindow() {
        if (askToSaveFestival()) {
            stage.close();
            LOGGER.info("Closed window");
        }
    }

    /**
     * Minimise la fenêtre.
     */
    @FXML
    private void minimizeWindow() {
        stage.setIconified(true);
        LOGGER.info("Minimized window");
    }

    /**
     * Ajoute un fichier récent à la liste et met à jour le menu des fichiers récents.
     * @param file Le fichier à ajouter
     */
    private void addRecentFile(File file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        if (recentFiles.size() > MAX_RECENT_FILES)
            recentFiles.remove(MAX_RECENT_FILES);
        recentFileMenu.getItems().clear();
        loadRecentFileMenu();
        LOGGER.info("Added recent file");
    }

    /**
     * Charge le menu des fichiers récents.
     */
    private void loadRecentFileMenu() {
        LOGGER.info("Loading recent files");
        for (File file : recentFiles) {
            MenuItem menu = new MenuItem(file.getName());
            menu.setOnAction(e -> openFestival(file));
            recentFileMenu.getItems().add(menu);
            LOGGER.info("Loaded recent files \"" + file.getName() + "\"");
        }
        LOGGER.info("Loaded recent files");
    }

    /**
     * Demande à l'utilisateur s'il souhaite sauvegarder le festival actuel s'il a été modifié.
     * @return true si l'utilisateur a choisi de sauvegarder ou de ne pas sauvegarder, false s'il a annulé
     */
    private boolean askToSaveFestival() {
        if (festivalHash == festival.hashCode()) {
            LOGGER.info("Festival\"" + festival.getName() + "\" didn't changed");
            return true;
        }
        LOGGER.info("Asking to save festival");
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                String.format("Le festival \"%s\" a été modifié, voulez-vous le sauvegarder ?", this.festival.getName()),
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL
        );
        alert.showAndWait();
        ButtonType result = alert.getResult();
        if (result.equals(ButtonType.YES)) {
            saveFestival();
            LOGGER.info("Saved festival \"" + festival.getName() + "\"");
        }
        else if (result.equals(ButtonType.CANCEL)) {
            LOGGER.info("User cancelled saving");
            return false;
        } ;
        return true;
    }

    /**
     * Charge un festival à partir d'un fichier.
     * @param festival Le festival à charger
     */
    private void loadFestival(Festival festival) {
        if (this.festival != null && !askToSaveFestival())
            return;
        LOGGER.info("Loading festival \"" + festival.getName() + "\"");
        File file = festival.getFile();
        if (file != null)
            addRecentFile(file);
        tryCatch(
            "Chargement du festival impossible",
            () -> {
                this.festival = festival;
                festivalHash = festival.hashCode();
                loadWorkspace(DEFAULT_WORKSPACE);
                LOGGER.info("Loaded festival \"" + festival.getName() + "\"");
        });
    }

    /**
     * Crée un nouveau festival avec des valeurs par défaut.
     */
    @FXML
    private void newFestival() {
        LOGGER.info("Creating new festival");
        tryCatch(
            "Création du nouveau festival impossible",
            () -> {
                loadFestival(new Festival(
                        "Nouveau festival",
                        LocalDateTime.now(),
                        0,
                        1,
                        "Quelque part sur Terre"
                ));
                LOGGER.info("Created new festival");
            });
    }

    /**
     * Ouvre un festival à partir d'un fichier sélectionné par l'utilisateur.
     */
    @FXML
    private void openFestival() {
        LOGGER.info("Opening festival from dialog");
        tryCatch(
            "Ouverture du fichier du festival impossible",
            () -> {
                File file = askFile("Open").showOpenDialog(stage);
                if (file == null) {
                    LOGGER.info("User cancelled opening");
                    return;
                }
                loadFestival(Festival.Festival(file));
                LOGGER.info("Opened another festival");
        });
    }

    /**
     * Ouvre un festival à partir du fichier spécifié.
     * @param file Le fichier du festival à ouvrir
     */
    private void openFestival(File file) {
        LOGGER.info("Opening a festival file");
        tryCatch(
    "Ouverture du fichier du festival impossible",
            () -> {
                loadFestival(Festival.Festival(file));
                LOGGER.info("Opened a festival file");
            }
        );
    }

    /**
     * Sauvegarde le festival actuel.
     */
    @FXML
    private void saveFestival() {
        LOGGER.info("Saving current festival");
        if (festival.getFile() == null) {
            LOGGER.info("Current festival without file, asking for one to save");
            saveFestivalAs();
        } else tryCatch(
    "Sauvegarde du festival impossible",
    "Festival sauvegardé",
            () -> {
                festival.save();
                festivalHash = festival.hashCode();
                LOGGER.info("Saving current festival");
            }
        );
    }

    /**
     * Sauvegarde le festival actuel sous un nouveau nom.
     */
    @FXML
    private void saveFestivalAs() {
        LOGGER.info("Saving festival as");
        File file = askFile("Enregistrer sous", festival.getName()).showSaveDialog(stage);
        if (file == null) {
            LOGGER.info("User cancelled save");
        } else tryCatch(
    "Sauvegarde du festival impossible",
    "Festival sauvegardé",
            () -> {
                festival.setFile(file);
                addRecentFile(file);
                festival.save();
                festivalHash = festival.hashCode();
                LOGGER.info("Saved festival as \"" + festival.getFile().getName() + "\"");
        });
    }

    /**
     * Ajoute un espace de travail au menu et configure son bouton.
     * @param register L'enregistrement de l'espace de travail à ajouter
     */
    public void addWorkspace(WorkspaceController.WorkspaceRegister register) {
        LOGGER.info("Adding workspace \"" + register.name + "\"");
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
                    () -> {
                        loadWorkspace(register);
                        LOGGER.info("Added workspace \"" + register.name + "\"");
                    }
            );
        });
    }

    /**
     * Charge l'espace de travail spécifié.
     * @param register L'enregistrement de l'espace de travail à charger
     * @throws IOException Si une erreur d'entrée/sortie se produit
     */
    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        LOGGER.info("Loading workspace \"" + register.name + "\"");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        currentWorkspaceController = fxmlLoader.getController();
        currentWorkspaceController.initialize(festival);
        stage.setTitle(String.format("%s - %s", APP_NAME, register.name));

        if (currentWorkspace != null)
            currentWorkspace.openButton.getStyleClass().remove(CURRENT_WORKSPACE_STYLECLASS);
        currentWorkspace = register;
        register.openButton.getStyleClass().add(CURRENT_WORKSPACE_STYLECLASS);

        LOGGER.info("Added workspace \"" + register.name + "\"");
    }

    /**
     * Ouvre le pdf du manuel de l'application.
     * Compatible avec les systèmes d'exploitation Linux et Windows.
     */
    @FXML
    private void onAboutClicked() {
        LOGGER.info("Opening manual");
        tryCatch(
    "Ouverture du manuel impossible",
            () -> {
                File file = new File("src/main/resources/com/musigma/manual.pdf");
                if (file.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        ProcessBuilder pb = new ProcessBuilder("evince", file.getAbsolutePath());
                        pb.start();
                    }
                }
                LOGGER.info("Opened manual");
            }
        );
    }
}
