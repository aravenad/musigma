package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import static com.musigma.utils.Dialogs.tryCatch;

public class IntTextField extends NotEmptyTextField {

    private boolean positive;
    private boolean notNull;

    public IntTextField() {
        super();
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\.|\\d)]*", ""));
            }
        });
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public boolean isValid() {
        if (super.isValid()) {
            int value = Integer.parseInt(input.getText());
            if (positive && value < 0){
                setError("La valeur doit être positive");
                return false;
            }
            else if (notNull && value == 0){
                setError("La valeur doit être non-nulle");
                return false;
            }
            return super.isValid();
        } else return false;
    }
    public void bindInt(String errrorMsg, Setter<Integer> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid)
                tryCatch(
                    errrorMsg,
                    () -> setter.accept(Integer.parseInt(input.getText()))
                );
        });
    }

    public void bindInt(String errrorMsg, int value, Setter<Integer> setter) {
        input.setText(Integer.toString(value));
        isValid();
        bindInt(errrorMsg, setter);
    }
}
