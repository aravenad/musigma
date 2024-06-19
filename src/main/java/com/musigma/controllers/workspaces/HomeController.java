package com.musigma.controllers.workspaces;

import com.calendarfx.view.TimeField;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.exception.FestivalException;
import com.musigma.utils.exceptionMethods.Setter;
import impl.com.calendarfx.view.NumericTextField;
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
    TextField festivalName;

    @FXML
    DatePicker festivalStartDate;

    // TODO: Use that field
    @FXML
    TimeField festivalStartTime;

    @FXML
    TextField festivalLocation;

    @FXML
    NumericTextField festivalArea;

    @FXML
    NumericTextField festivalLocationPrice;

    @Override
    public void initialize(Festival festival) {
        useNotEmptyText(festivalName, festival.getName(), festival::setName);
        useNotEmptyText(festivalLocation, festival.getLocation(), festival::setLocation);
        usePositiveNotNull(festivalArea, festival.getArea(), festival::setArea);
        usePositive(festivalLocationPrice, festival.getLocationPrice(), festival::setArea);
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

    private void usePositive(NumericTextField tf, double defaultValue, Setter<Double> setter) {
        useNotEmptyText(tf, String.format("%d", (long) defaultValue), (vString) -> {
            double v = Double.parseDouble(vString);
            if (v > 0)
                setter.accept(v);
        });
    }

    private void usePositiveNotNull(NumericTextField tf, double defaultValue, Setter<Double> setter) {
        useNotEmptyText(tf, String.format("%d", (long) defaultValue), (vString) -> {
            double v = Double.parseDouble(vString);
            if (v >= 0)
                setter.accept(v);
        });
    }

    private void useNotEmptyText(TextField tf, String defaultValue, Setter<String> setter) {
        tf.setText(defaultValue);
        tf.textProperty().addListener(e -> {
            String value = tf.getText();
            if (!value.isEmpty()) {
                tryCatch(
                "Impossible de mettre à jour ",
                    () -> setter.accept(value)
                );
            }
        });
    }
}
