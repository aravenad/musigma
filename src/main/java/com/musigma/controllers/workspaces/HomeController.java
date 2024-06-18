package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import impl.com.calendarfx.view.NumericTextField;
import impl.com.calendarfx.view.TimeFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HomeController extends WorkspaceController {
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
        "Home",
        "/com/musigma/images/icons/home.png",
        "/com/musigma/views/home-view.fxml"
    );
    public DatePicker festivalStart;

    @FXML
    TextField festivalName;

    @FXML
    DatePicker festivalStart;

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
        usePositive(festivalLocationPrice, (double) festival.getLocationPrice(), (v) -> festival.setArea(v));
    }

    @FunctionalInterface
    public interface ExceptionConsumer<T> {
        void accept(T t) throws Exception;
    }

    private void usePositive(NumericTextField tf, Double defaultValue, ExceptionConsumer<Double> setter) {
        useNotEmptyText(tf, String.format("%g", defaultValue), (vString) -> {
            Double v = Double.parseDouble(vString);
            if (v > 0)
                setter.accept(v);
        });
    }

    private void usePositiveNotNull(NumericTextField tf, Double defaultValue, ExceptionConsumer<Double> setter) {
        useNotEmptyText(tf, String.format("%g", defaultValue), (vString) -> {
            Double v = Double.parseDouble(vString);
            if (v > 0)
                setter.accept(v);
        });
    }

    private void useNotEmptyText(TextField tf, String defaultValue, ExceptionConsumer<String> setter) {
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
