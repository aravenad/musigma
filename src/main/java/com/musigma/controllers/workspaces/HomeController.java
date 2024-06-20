package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Festival;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Contrôleur de l'espace de travail Home.
 */
public class HomeController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     * Définit le nom, l'icône et la vue de l'espace de travail.
     *
     * @see com.musigma.controllers.WorkspaceController
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Home",
            "/com/musigma/images/icons/home.png",
            "/com/musigma/views/home-view.fxml"
    );

    @FXML
    RequiredTextField festivalName, festivalLocation; // Champs de texte requis pour le nom et l'emplacement du festival

    @FXML
    DatePicker festivalStartDate; // Sélecteur de date pour la date de début du festival

    @FXML
    FloatTextField festivalArea, festivalLocationPrice; // Champs de texte flottants pour la surface et le prix de l'emplacement du festival

    /**
     * Initialise le contrôleur avec les informations du festival.
     *
     * @param festival Le festival à initialiser
     */
    @Override
    public void initialize(Festival festival) {
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

        // Configuration du sélecteur de date pour désactiver les dates antérieures à aujourd'hui
        festivalStartDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        festivalStartDate.setValue(festival.getStart().toLocalDate());
        festivalStartDate.valueProperty().addListener(e -> {
            tryCatch(
                    "Changement de la date de départ impossible",
                    () -> festival.setStart(festivalStartDate.getValue().atStartOfDay())
            );
        });
    }
}
