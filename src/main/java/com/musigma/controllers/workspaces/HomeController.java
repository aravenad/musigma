package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Festival;
import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.util.function.Supplier;

import static com.musigma.controllers.Dialogs.*;

/**
 * Contrôleur de l'espace de travail Home : informations générales du festival (UC1).
 */
public class HomeController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     * Définit le nom, l'icône et la vue de l'espace de travail.
     *
     * @see com.musigma.controllers.WorkspaceController
     */
    public static final WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Accueil",
            "/com/musigma/images/icons/home.png",
            "/com/musigma/views/home-view.fxml",
            "Renseignez les informations générales du festival : dates, lieu, surface, prix de location et scènes. Les modifications sont prises en compte immédiatement."
    );

    @FXML
    RequiredTextField festivalName, festivalLocation; // Champs de texte requis pour le nom et l'emplacement du festival

    @FXML
    DatePicker festivalStartDate, festivalEndDate; // Sélecteurs du premier et du dernier jour du festival

    @FXML
    FloatTextField festivalArea, festivalLocationPrice; // Champs de texte flottants pour la surface et le prix de l'emplacement du festival

    @FXML
    ListView<String> scenesList; // Scènes du festival

    /**
     * Vrai pendant qu'on remet les sélecteurs de date à jour depuis le modèle, pour ignorer leurs écouteurs.
     */
    private boolean refreshingDates = false;

    /**
     * Initialise le contrôleur avec les informations du festival.
     *
     * @param festival Le festival à initialiser
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        // Liaison des champs de texte avec les propriétés du festival
        festivalName.bind(
                "Mise à jour du nom du festival impossible",
                festival.getName(), festival::setName
        );
        festivalLocation.bind(
                "Mise à jour de l'emplacement du festival impossible",
                festival.getLocation(), festival::setLocation
        );
        festivalArea.bindValue(
                "Mise à jour de l'aire de l'emplacement du festival impossible",
                festival.getArea(), festival::setArea
        );
        festivalLocationPrice.bindValue(
                "Mise à jour du prix de l'emplacement du festival impossible",
                festival.getLocationPrice(), festival::setLocationPrice
        );

        // Le premier jour ne peut pas être passé, le dernier jour ne peut pas précéder le premier
        festivalStartDate.setDayCellFactory(picker -> disabledBefore(LocalDate::now));
        festivalEndDate.setDayCellFactory(picker -> disabledBefore(() -> festival.getStart().toLocalDate()));
        refreshDates();
        bindDate(
                festivalStartDate,
                "Changement de la date de début impossible",
                date -> festival.setStart(date.atStartOfDay())
        );
        bindDate(
                festivalEndDate,
                "Changement de la date de fin impossible",
                date -> festival.setEnd(date.plusDays(1).atStartOfDay())
        );

        refreshScenes();
    }

    /**
     * Crée une cellule de calendrier désactivant les jours antérieurs à une date.
     *
     * @param firstDay la date du premier jour sélectionnable
     * @return la cellule de calendrier
     */
    private DateCell disabledBefore(Supplier<LocalDate> firstDay) {
        return new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(firstDay.get()));
            }
        };
    }

    /**
     * Transmet chaque date choisie au modèle, puis réaffiche les dates du modèle,
     * ce qui annule un choix refusé et répercute le décalage de la date de fin.
     *
     * @param picker   le sélecteur de date
     * @param errorMsg le message d'erreur si le modèle refuse la date
     * @param setter   la modification du modèle
     */
    private void bindDate(DatePicker picker, String errorMsg, Setter<LocalDate> setter) {
        picker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (refreshingDates)
                return;
            if (newDate == null)
                showError(errorMsg + " :\nDate invalide");
            else
                tryCatch(errorMsg, () -> setter.accept(newDate));
            refreshDates();
        });
    }

    /**
     * Affiche le premier et le dernier jour du festival. La fin du festival est exclue, d'où le jour de moins.
     */
    private void refreshDates() {
        refreshingDates = true;
        festivalStartDate.setValue(festival.getStart().toLocalDate());
        festivalEndDate.setValue(festival.getEnd().minusNanos(1).toLocalDate());
        refreshingDates = false;
    }

    /**
     * Affiche les scènes du festival.
     */
    private void refreshScenes() {
        scenesList.getItems().setAll(festival.getScenes());
    }

    /**
     * Ajoute une scène dont le nom est demandé à l'utilisateur.
     */
    @FXML
    private void addScene() {
        askText("Ajouter une scène", "Nom de la nouvelle scène :", "").ifPresent(name -> tryCatch(
                "Ajout de la scène impossible",
                () -> {
                    festival.addScene(name);
                    refreshScenes();
                }
        ));
    }

    /**
     * Renomme la scène sélectionnée.
     */
    @FXML
    private void renameScene() {
        String scene = scenesList.getSelectionModel().getSelectedItem();
        if (scene == null) {
            showError("Sélectionnez la scène à renommer");
            return;
        }
        askText("Renommer la scène", "Nouveau nom de la scène :", scene).ifPresent(name -> tryCatch(
                "Renommage de la scène impossible",
                () -> {
                    festival.renameScene(scene, name);
                    refreshScenes();
                }
        ));
    }

    /**
     * Supprime la scène sélectionnée.
     */
    @FXML
    private void removeScene() {
        String scene = scenesList.getSelectionModel().getSelectedItem();
        if (scene == null) {
            showError("Sélectionnez la scène à supprimer");
            return;
        }
        tryCatch(
                "Suppression de la scène impossible",
                () -> {
                    festival.removeScene(scene);
                    refreshScenes();
                }
        );
    }
}
