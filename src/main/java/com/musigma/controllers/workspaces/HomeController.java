package com.musigma.controllers.workspaces;

import com.calendarfx.view.TimeField;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.exception.FestivalException;
import impl.com.calendarfx.view.NumericTextField;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        super.initialize(festival);
        useNotEmptyText(festivalName, festival.getName(), (String v) -> festival.setName(v));
        useNotEmptyText(festivalLocation, festival.getLocation(), (String v) -> festival.setLocation(v));
        usePositiveNotNull(festivalArea, festival.getArea(), (v) -> festival.setArea(v));
        usePositive(festivalLocationPrice, festival.getLocationPrice(), (v) -> festival.setArea(v));
        festivalStartDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        festivalStartDate.setValue(festival.getStart().toLocalDate());
        festivalStartDate.valueProperty().addListener(e -> {
            try {
                festival.setStart(festivalStartDate.getValue().atStartOfDay());
            } catch (FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @FunctionalInterface
    public interface Setter<T> {
        void accept(T t) throws Exception;
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
                try {
                    setter.accept(value);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
