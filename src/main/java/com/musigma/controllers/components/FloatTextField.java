package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;

import static com.musigma.utils.Dialogs.tryCatch;

/**
 * A custom JavaFX component extending RequiredTextField to handle float inputs with additional validation.
 */
public class FloatTextField extends RequiredTextField {

    private boolean positive;
    private boolean notNull;

    /**
     * Constructor that initializes the component and sets up the input field to only accept float values.
     */
    public FloatTextField() {
        super();
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+(?:\\.\\d+)?")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\.|\\d)]*", ""));
            }
        });
    }

    /**
     * Sets the float value of the input field.
     *
     * @param value the float value to set
     */
    public void setValue(float value) {
        input.setText(Float.toString(value).replaceAll("0*$", "").replaceAll("\\.$", ""));
    }

    /**
     * Gets the float value from the input field.
     *
     * @return the float value in the input field
     */
    public float getValue() {
        return Float.parseFloat(input.getText());
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
     * Validates the input field for float value constraints.
     *
     * @return true if the input is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        if (super.isValid()) {
            float value = Float.parseFloat(input.getText());
            if (positive && value < 0){
                setError("La valeur doit être positive");
                return false;
            } else if (notNull && value == 0){
                setError("La valeur doit être non-nulle");
                return false;
            }
            return super.isValid();
        } else {
            return false;
        }
    }

    /**
     * Binds the input field to a float setter with an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param setter   the setter to bind the input value to
     */
    public void bindFloat(String errorMsg, Setter<Float> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid())
                tryCatch(
                        errorMsg,
                        () -> setter.accept(getValue())
                );
        });
    }

    /**
     * Binds the input field to a float setter with an initial value and an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param value    the initial value to set in the input field
     * @param setter   the setter to bind the input value to
     */
    public void bindFloat(String errorMsg, Float value, Setter<Float> setter) {
        setValue(value);
        isValid();
        bindFloat(errorMsg, setter);
    }
}
