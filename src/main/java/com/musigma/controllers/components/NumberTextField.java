package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;

import static com.musigma.controllers.Dialogs.tryCatch;

public abstract class NumberTextField<T extends Number> extends RequiredTextField {

    private boolean positive;
    private boolean notNull;

    /**
     * Constructor that loads the FXML file and sets up the component.
     */
    public NumberTextField() {
        super();
    }

    /**
     * Constructor that loads the FXML file and sets up the component.
     */
    public NumberTextField(boolean positive, boolean notNull) {
        super();
        this.positive = positive;
        this.notNull = notNull;
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

    /**
     * Validates the input field.
     *
     * @return true if the input field is valid, false otherwise
     */
    public boolean isValid() {
        if (super.isValid()) {
            String stringValue = getText();
            if (stringValue.replaceAll("[+-]", "").isBlank()) {
                setError("Valeur requise");
                return false;
            }
            double value = Double.parseDouble(stringValue);
            if (positive && value < 0) {
                setError("Valeur positive");
                return false;
            } else if (notNull && value == 0) {
                setError("Valeur non-nulle");
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public abstract void setValue(T value);
    public abstract T getValue();

    public void bindValue(String errorMsg, Setter<T> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid())
                tryCatch(
                    errorMsg,
                    () -> setter.accept(getValue())
                );
        });
    }

    public void bindValue(String errorMsg, T value, Setter<T> setter) {
        bindValue(errorMsg, setter);
        setValue(value);
    }
}
