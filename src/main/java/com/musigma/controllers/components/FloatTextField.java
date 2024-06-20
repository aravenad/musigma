package com.musigma.controllers.components;

/**
 * A custom JavaFX component extending RequiredTextField to handle float inputs with additional validation.
 */
public class FloatTextField extends NumberTextField<Float> {

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

    public FloatTextField(boolean positive, boolean notNull) {
        super(positive, notNull);
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+(?:\\.\\d+)?")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\.|\\d)]*", ""));
            }
        });
    }

    @Override
    public void setValue(Float value) {
        input.setText(Float.toString(value).replaceAll("0*$", "").replaceAll("\\.$", ""));
    }

    @Override
    public Float getValue() {
        return Float.parseFloat(input.getText());
    }
}
