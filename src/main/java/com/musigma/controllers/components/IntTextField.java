package com.musigma.controllers.components;

/**
 * A custom JavaFX component extending RequiredTextField to handle integer inputs with additional validation.
 */
public class IntTextField extends NumberTextField<Integer> {

    /**
     * Constructor that initializes the component and sets up the input field to only accept float values.
     */
    public IntTextField() {
        super();
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\d)]*", ""));
            }
        });
    }

    public IntTextField(boolean positive, boolean notNull) {
        super(positive, notNull);
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\d)]*", ""));
            }
        });
    }

    @Override
    public void setValue(Integer value) {
        input.setText(Float.toString(value).replaceAll("^0*", ""));
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(input.getText());
    }
}
