package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;

import static com.musigma.utils.Dialogs.tryCatch;

/**
 * A custom JavaFX component extending RequiredTextField to handle integer inputs with additional validation.
 */
public class IntTextField extends RequiredTextField {

    private boolean positive;
    private boolean notNull;

    /**
     * Constructor that initializes the component and sets up the input field to only accept integer values.
     */
    public IntTextField() {
        super();
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\d)]*", ""));
            }
        });
    }

    /**
     * Sets the integer value of the input field.
     *
     * @param value the integer value to set
     */
    public void setValue(int value) {
        input.setText(Integer.toString(value));
    }

    /**
     * Gets the integer value from the input field.
     *
     * @return the integer value in the input field
     */
    public int getValue() {
        return Integer.parseInt(input.getText());
    }

    /**
     * Checks if the field is not null.
     *
     * @return true if the field must not be null, false otherwise
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Sets whether the field must not be null.
     *
     * @param notNull true if the field must not be null, false otherwise
     */
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    /**
     * Checks if the field must have positive values.
     *
     * @return true if the field must have positive values, false otherwise
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * Sets whether the field must have positive values.
     *
     * @param positive true if the field must have positive values, false otherwise
     */
    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    /**
     * Validates the input field for integer value constraints.
     *
     * @return true if the input is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        if (super.isValid()) {
            int value = Integer.parseInt(input.getText());
            if (positive && value < 0) {
                setError("La valeur doit être positive");
                return false;
            } else if (notNull && value == 0) {
                setError("La valeur doit être non-nulle");
                return false;
            }
            return super.isValid();
        } else {
            return false;
        }
    }

    /**
     * Binds the input field to an integer setter with an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param setter   the setter to bind the input value to
     */
    public void bindInt(String errorMsg, Setter<Integer> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid)
                tryCatch(
                        errorMsg,
                        () -> setter.accept(getValue())
                );
        });
    }

    /**
     * Binds the input field to an integer setter with an initial value and an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param value    the initial value to set in the input field
     * @param setter   the setter to bind the input value to
     */
    public void bindInt(String errorMsg, int value, Setter<Integer> setter) {
        setValue(value);
        isValid();
        bindInt(errorMsg, setter);
    }
}
