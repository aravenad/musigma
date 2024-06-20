package com.musigma.controllers;

import com.musigma.controllers.workspaces.*;
import com.musigma.models.Festival;
import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.TypeTicketException;
import javafx.event.ActionEvent;
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

/**
 * Le contrôleur principal de l'application Musigma, gère l'initialisation,
 * la navigation entre les espaces de travail, et la gestion des fichiers récents.
 */
public class MainController {

    private static final String APP_NAME = "Musigma"; // Nom de l'application
    private static final int MAX_RECENT_FILES = 10; // Nombre maximum de fichiers récents à conserver
    private static final String STATE_FILEPATH = "previousSession.ser"; // Chemin du fichier d'état de la session précédente

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
    }

    /**
     * Charge l'état de la session précédente depuis le fichier d'état.
     */
    private void loadState() {
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
    }

    /**
     * Sauvegarde l'état actuel de l'application dans le fichier d'état.
     */
    private void saveState() {
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
    }

    /**
     * Ferme la fenêtre après avoir demandé si l'utilisateur souhaite sauvegarder le festival.
     */
    @FXML
    private void closeWindow() {
        if (askToSaveFestival())
            stage.close();
    }

    /**
     * Minimise la fenêtre.
     */
    @FXML
    private void minimizeWindow() {
        stage.setIconified(true);
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
    }

    /**
     * Charge le menu des fichiers récents.
     */
    private void loadRecentFileMenu() {
        for (File file : recentFiles) {
            MenuItem menu = new MenuItem(file.getName());
            menu.setOnAction(e -> openFestival(file));
            recentFileMenu.getItems().add(menu);
        }
    }

    /**
     * Demande à l'utilisateur s'il souhaite sauvegarder le festival actuel s'il a été modifié.
     * @return true si l'utilisateur a choisi de sauvegarder ou de ne pas sauvegarder, false s'il a annulé
     */
    private boolean askToSaveFestival() {
        if (festival.getFile() != null && festivalHash == festival.hashCode())
            return true;
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                String.format("Le festival \"%s\" a été modifié, voulez-vous le sauvegarder ?", this.festival.getName()),
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

    /**
     * Charge un festival à partir d'un fichier.
     * @param festival Le festival à charger
     */
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

    /**
     * Crée un nouveau festival avec des valeurs par défaut.
     */
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

    /**
     * Ouvre un festival à partir d'un fichier sélectionné par l'utilisateur.
     */
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

    /**
     * Ouvre un festival à partir du fichier spécifié.
     * @param file Le fichier du festival à ouvrir
     */
    private void openFestival(File file) {
        tryCatch(
            "Ouverture du fichier du festival impossible",
            () -> loadFestival(Festival.Festival(file))
        );
    }

    /**
     * Sauvegarde le festival actuel.
     */
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

    /**
     * Sauvegarde le festival actuel sous un nouveau nom.
     */
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

    /**
     * Ajoute un espace de travail au menu et configure son bouton.
     * @param register L'enregistrement de l'espace de travail à ajouter
     */
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

    /**
     * Charge l'espace de travail spécifié.
     * @param register L'enregistrement de l'espace de travail à charger
     * @throws IOException Si une erreur d'entrée/sortie se produit
     * @throws FestivalException Si une erreur liée au festival se produit
     */
    public void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException, TypeTicketException {
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

    public void onAboutClicked(ActionEvent actionEvent){
        try {
            File file = new File("src/main/resources/com/musigma/manual.pdf");
            if (file.exists()) {
                ProcessBuilder pb = new ProcessBuilder("evince", file.getAbsolutePath());
                pb.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
