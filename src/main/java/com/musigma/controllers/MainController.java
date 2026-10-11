package com.musigma.controllers;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.musigma.controllers.workspaces.*;
import com.musigma.models.Festival;
import com.musigma.utils.Log;
import com.musigma.utils.Settings;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.musigma.controllers.Dialogs.askFile;
import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Le contrôleur principal de l'application Musigma, gère l'initialisation,
 * la navigation entre les espaces de travail, les fichiers récents et les préférences d'affichage.
 */
public class MainController {

    /**
     * Chemin de l'icône de l'application.
     */
    protected static final String ICON_PATH = "/com/musigma/images/logo_full.png";
    /**
     * Logger pour les messages de log.
     */
    private static final Logger LOGGER = Log.getLogger(MainController.class);
    /**
     * Nom de l'application.
     */
    private static final String APP_NAME = "Musigma";
    /**
     * Nombre maximum de fichiers récents à conserver.
     */
    private static final int MAX_RECENT_FILES = 10;
    /**
     * Chemin du manuel utilisateur dans les ressources de l'application.
     */
    private static final String GUIDE_PATH = "/com/musigma/guide/guide.pdf";
    /**
     * Taille de police correspondant à un zoom de 100 %, en pixels.
     */
    private static final double BASE_FONT_SIZE = 14;

    /**
     * Espaces de travail disponibles, dans l'ordre du menu.
     */
    private static final List<WorkspaceController.WorkspaceRegister> WORKSPACES = List.of(
            HomeController.REGISTER,
            ArtistController.REGISTER,
            CalendarController.REGISTER,
            StockController.REGISTER,
            TicketController.REGISTER,
            AccountingController.REGISTER
    );

    private static final WorkspaceController.WorkspaceRegister DEFAULT_WORKSPACE = HomeController.REGISTER; // Espace de travail par défaut

    private static final String CURRENT_WORKSPACE_STYLECLASS = "currentWorkspace"; // Classe CSS pour l'espace de travail actuel

    private final Settings settings = Settings.user(); // Préférences de l'utilisateur
    private Festival festival; // Le festival actuel
    private List<File> recentFiles; // Liste des fichiers récents
    private WorkspaceController.WorkspaceRegister currentWorkspace; // Espace de travail actuel
    private Stage stage; // La fenêtre principale
    private HostServices hostServices; // Services de la plateforme, pour ouvrir le manuel

    @FXML
    private VBox pageMenu; // Menu de la page
    @FXML
    private Menu recentFileMenu; // Menu des fichiers récents
    @FXML
    private StackPane workspace; // Espace de travail
    @FXML
    private HBox helpBar; // Zone d'aide contextuelle
    @FXML
    private Label helpText; // Texte de l'aide contextuelle
    @FXML
    private CheckMenuItem darkThemeItem, helpItem; // Choix du thème et de l'affichage de l'aide

    /**
     * Initialise le contrôleur principal avec la fenêtre spécifiée.
     *
     * @param stage        La fenêtre principale
     * @param hostServices Les services de la plateforme, pour ouvrir le manuel
     */
    public void initialize(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
        recentFiles = new ArrayList<>(settings.getRecentFiles(MAX_RECENT_FILES));
        loadRecentFileMenu();
        // Fermeture demandée par le système (bouton de la fenêtre, Alt+F4...) : proposer d'enregistrer
        stage.setOnCloseRequest(e -> {
            if (!askToSaveFestival())
                e.consume();
        });
        for (WorkspaceController.WorkspaceRegister register : WORKSPACES)
            addWorkspace(register);

        darkThemeItem.setSelected(settings.isDarkTheme());
        applyTheme();
        helpItem.setSelected(settings.isHelpVisible());
        applyHelpVisibility();
        applyZoom(settings.getZoom());

        if (recentFiles.isEmpty())
            newFestival();
        else
            openFestival(recentFiles.get(0));

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(ICON_PATH))));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
        LOGGER.info("Loaded main controller");
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
     * Ajoute un fichier en tête des fichiers récents et met à jour le menu et les préférences.
     *
     * @param file Le fichier à ajouter
     */
    private void addRecentFile(File file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        if (recentFiles.size() > MAX_RECENT_FILES)
            recentFiles.remove(MAX_RECENT_FILES);
        settings.setRecentFiles(recentFiles, MAX_RECENT_FILES);
        loadRecentFileMenu();
    }

    /**
     * Affiche les fichiers récents dans le menu.
     */
    private void loadRecentFileMenu() {
        recentFileMenu.getItems().clear();
        for (File file : recentFiles) {
            MenuItem menu = new MenuItem(file.getName());
            menu.setOnAction(e -> openFestival(file));
            recentFileMenu.getItems().add(menu);
        }
        recentFileMenu.setDisable(recentFiles.isEmpty());
    }

    /**
     * Demande à l'utilisateur s'il souhaite sauvegarder le festival actuel s'il a été modifié.
     *
     * @return true si le festival peut être quitté (enregistré ou abandonné volontairement),
     * false si l'utilisateur a annulé ou si l'enregistrement n'a pas abouti
     */
    private boolean askToSaveFestival() {
        if (!festival.estModifie()) {
            LOGGER.fine("Festival \"" + festival.getNom() + "\" didn't change");
            return true;
        }
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                String.format("Le festival \"%s\" a été modifié, voulez-vous l'enregistrer ?", this.festival.getNom()),
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL
        );
        Dialogs.style(alert.getDialogPane());
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.YES) {
            saveFestival();
            if (festival.estModifie()) {
                LOGGER.info("Festival \"" + festival.getNom() + "\" was not saved");
                return false;
            }
        } else if (result != ButtonType.NO) {
            LOGGER.info("User cancelled saving");
            return false;
        }
        return true;
    }

    /**
     * Affiche un festival, après avoir proposé d'enregistrer le festival actuel.
     *
     * @param festival Le festival à charger
     */
    private void loadFestival(Festival festival) {
        if (this.festival != null && !askToSaveFestival())
            return;
        File file = festival.getFichier();
        if (file != null)
            addRecentFile(file);
        tryCatch(
                "Chargement du festival impossible",
                () -> {
                    this.festival = festival;
                    festival.marquerEnregistre();
                    currentWorkspace = null;
                    loadWorkspace(DEFAULT_WORKSPACE);
                    LOGGER.info("Loaded festival \"" + festival.getNom() + "\"");
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
                        LocalDate.now().atStartOfDay(),
                        0,
                        1,
                        "Quelque part sur Terre"
                ))
        );
    }

    /**
     * Ouvre un festival à partir d'un fichier sélectionné par l'utilisateur.
     */
    @FXML
    private void openFestival() {
        File file = askFile("Ouvrir un festival").showOpenDialog(stage);
        if (file != null)
            openFestival(file);
    }

    /**
     * Ouvre un festival à partir du fichier spécifié. En cas d'échec au démarrage, un nouveau festival est créé.
     *
     * @param file Le fichier du festival à ouvrir
     */
    private void openFestival(File file) {
        tryCatch(
                "Ouverture du fichier du festival impossible",
                () -> loadFestival(Festival.charger(file))
        );
        if (festival == null)
            newFestival();
    }

    /**
     * Enregistre le festival actuel, dans son fichier s'il en a un, sinon dans un fichier à choisir.
     */
    @FXML
    private void saveFestival() {
        if (festival.getFichier() == null)
            saveFestivalAs();
        else tryCatch(
                "Enregistrement du festival impossible",
                "Festival enregistré",
                festival::enregistrer
        );
    }

    /**
     * Enregistre le festival actuel dans un fichier à choisir.
     */
    @FXML
    private void saveFestivalAs() {
        File file = askFile("Enregistrer sous", festival.getNom()).showSaveDialog(stage);
        if (file != null) tryCatch(
                "Enregistrement du festival impossible",
                "Festival enregistré",
                () -> {
                    festival.setFichier(file);
                    festival.enregistrer();
                    addRecentFile(file);
                });
    }

    /**
     * Ajoute un espace de travail au menu de gauche, avec une infobulle décrivant l'espace.
     *
     * @param register L'enregistrement de l'espace de travail à ajouter
     */
    private void addWorkspace(WorkspaceController.WorkspaceRegister register) {
        ImageView icon = new ImageView();
        icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(register.iconPath))));
        icon.setFitHeight(32);
        icon.setFitWidth(32);
        icon.setPreserveRatio(true);
        icon.setPickOnBounds(true);

        Label pageLabel = new Label(register.name);
        pageLabel.setStyle("-fx-font-size: 0.75em;");

        VBox pageButtonBox = new VBox(icon, pageLabel);
        pageButtonBox.setAlignment(Pos.CENTER);
        pageButtonBox.setPadding(new Insets(8));
        Tooltip tooltip = new Tooltip(register.name + "\n" + register.help);
        tooltip.setWrapText(true);
        tooltip.setPrefWidth(320);
        Tooltip.install(pageButtonBox, tooltip);

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
     * Charge l'espace de travail spécifié et affiche son aide.
     *
     * @param register L'enregistrement de l'espace de travail à charger
     * @throws IOException Si la vue de l'espace de travail ne peut pas être chargée
     */
    private void loadWorkspace(WorkspaceController.WorkspaceRegister register) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(register.viewPath));
        workspace.getChildren().setAll((Node) fxmlLoader.load());
        WorkspaceController controller = fxmlLoader.getController();
        controller.initialize(festival);
        stage.setTitle(String.format("%s - %s - %s", APP_NAME, festival.getNom(), register.name));
        helpText.setText(register.help);

        for (WorkspaceController.WorkspaceRegister other : WORKSPACES)
            other.openButton.getStyleClass().remove(CURRENT_WORKSPACE_STYLECLASS);
        currentWorkspace = register;
        register.openButton.getStyleClass().add(CURRENT_WORKSPACE_STYLECLASS);
        LOGGER.info("Loaded workspace \"" + register.name + "\"");
    }

    /**
     * Bascule entre le thème clair et le thème sombre.
     */
    @FXML
    private void toggleDarkTheme() {
        settings.setDarkTheme(darkThemeItem.isSelected());
        applyTheme();
    }

    /**
     * Applique le thème choisi à toutes les fenêtres de l'application.
     */
    private void applyTheme() {
        Application.setUserAgentStylesheet(darkThemeItem.isSelected()
                ? new CupertinoDark().getUserAgentStylesheet()
                : new CupertinoLight().getUserAgentStylesheet());
    }

    /**
     * Affiche ou masque l'aide contextuelle.
     */
    @FXML
    private void toggleHelp() {
        settings.setHelpVisible(helpItem.isSelected());
        applyHelpVisibility();
    }

    /**
     * Masque l'aide contextuelle depuis sa croix de fermeture.
     */
    @FXML
    private void hideHelp() {
        helpItem.setSelected(false);
        toggleHelp();
    }

    private void applyHelpVisibility() {
        helpBar.setVisible(helpItem.isSelected());
        helpBar.setManaged(helpItem.isSelected());
    }

    /**
     * Agrandit l'interface.
     */
    @FXML
    private void zoomIn() {
        applyZoom(settings.setZoom(settings.getZoom() + Settings.ZOOM_STEP));
    }

    /**
     * Réduit l'interface.
     */
    @FXML
    private void zoomOut() {
        applyZoom(settings.setZoom(settings.getZoom() - Settings.ZOOM_STEP));
    }

    /**
     * Rétablit la taille normale de l'interface.
     */
    @FXML
    private void resetZoom() {
        applyZoom(settings.setZoom(100));
    }

    /**
     * Applique un zoom à la fenêtre principale et aux boîtes de dialogue.
     *
     * @param zoom le zoom en pourcentage
     */
    private void applyZoom(int zoom) {
        String style = String.format("-fx-font-size: %.1fpx;", BASE_FONT_SIZE * zoom / 100);
        pageMenu.getScene().getRoot().setStyle(style);
        Dialogs.setRootStyle(style);
    }

    /**
     * Ouvre le pdf du manuel de l'application avec le lecteur par défaut du système.
     * Le manuel est extrait des ressources vers un fichier temporaire, ce qui fonctionne aussi depuis le jar.
     */
    @FXML
    private void onAboutClicked() {
        tryCatch(
                "Ouverture du manuel impossible",
                () -> {
                    Path guide = extractResource(GUIDE_PATH, ".pdf");
                    hostServices.showDocument(guide.toUri().toString());
                    LOGGER.info("Opened manual");
                }
        );
    }

    /**
     * Copie une ressource de l'application dans un fichier temporaire supprimé à la fermeture.
     *
     * @param resourcePath le chemin de la ressource
     * @param suffix       l'extension du fichier temporaire
     * @return le chemin du fichier temporaire
     * @throws IOException si la ressource est introuvable ou ne peut pas être copiée
     */
    static Path extractResource(String resourcePath, String suffix) throws IOException {
        try (InputStream resource = MainController.class.getResourceAsStream(resourcePath)) {
            if (resource == null)
                throw new FileNotFoundException("Ressource introuvable : " + resourcePath);
            Path file = Files.createTempFile("musigma-", suffix);
            file.toFile().deleteOnExit();
            Files.copy(resource, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        }
    }
}
