package com.musigma.controllers.workspaces;

import com.calendarfx.view.TimeField;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.NotEmptyTextField;
import com.musigma.models.Festival;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

import static com.musigma.utils.Dialogs.tryCatch;

public class HomeController extends WorkspaceController {
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
        "Home",
        "/com/musigma/images/icons/home.png",
        "/com/musigma/views/home-view.fxml"
    );

    @FXML
    NotEmptyTextField festivalName, festivalLocation;

    @FXML
    DatePicker festivalStartDate;

    // TODO: Use that field
    @FXML
    TimeField festivalStartTime;

    @FXML
    FloatTextField festivalArea, festivalLocationPrice;

    @Override
    public void initialize(Festival festival) {
        festivalName.bind(
    "Mise à jour du nom du festival impossible",
            festival.getName(), festival::setName
        );
        festivalLocation.bind(
                "Mise à jour de l'emplacement du festival impossible",
                festival.getLocation(), festival::setLocation
        );
        festivalArea.bindFloat(
                "Mise à jour de l'aire de l'emplacement du festival impossible",
                festival.getArea(), festival::setArea
        );
        festivalLocationPrice.bindFloat(
                "Mise à jour du prix de l'emplacement du festival impossible",
                festival.getLocationPrice(), festival::setLocationPrice
        );

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
