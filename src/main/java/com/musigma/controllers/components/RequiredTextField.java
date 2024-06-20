package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static com.musigma.utils.Dialogs.tryCatch;

/**
 * A custom JavaFX component representing a required text field with error handling.
 */
public class RequiredTextField extends GridPane {

    private static final String VIEW_PATH = "/com/musigma/views/components/error-text-field.fxml";

    @FXML
    protected TextField input;
    @FXML
    protected Label error;
    @FXML
    protected VBox errorBox;

    protected boolean isValid = false;

    /**
     * Constructor that loads the FXML file and sets up the component.
     */
    public RequiredTextField() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_PATH));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(RequiredTextField.this);
        tryCatch(
                String.format("Chargement du composant %s impossible", getClass().getSimpleName()),
                fxmlLoader::load
        );
        input.setOnKeyTyped(e -> isValid());
        isValid();
    }

    /**
     * Requests focus on the input field.
     */
    public void requestFocus() {
        input.requestFocus();
    }

    /**
     * Sets an error message and applies error styling.
     *
     * @param error the error message to display
     */
    public void setError(String error) {
        this.error.setText("* " + error);
        input.setStyle("-color-accent-emphasis: -color-danger-fg;-color-border-default: -color-danger-fg");
        errorBox.setVisible(true);
        isValid = false;
    }

    /**
     * Clears the error message and removes error styling.
     */
    public void clearError() {
        if (isValid) return;
        input.setStyle("");
        errorBox.setVisible(false);
        isValid = true;
    }

    /**
     * Checks if the input field is empty.
     *
     * @return true if the input field is empty, false otherwise
     */
    protected boolean isEmpty() {
        return isValid;
    }

    /**
     * Validates the input field.
     *
     * @return true if the input field is valid, false otherwise
     */
    public boolean isValid() {
        if (input.getText().isEmpty()) {
            setError("Valeur requise");
            return false;
        } else {
            clearError();
            return true;
        }
    }

    /**
     * Sets the text of the input field.
     *
     * @param text the text to set
     */
    public void setText(String text) {
        input.setText(text);
        isValid();
    }

    /**
     * Gets the text from the input field.
     *
     * @return the text in the input field
     */
    public String getText() {
        return input.getText();
    }

    /**
     * Sets the prompt text of the input field.
     *
     * @param promptText the prompt text to set
     */
    public void setPromptText(String promptText) {
        input.setPromptText(promptText);
    }

    /**
     * Gets the prompt text of the input field.
     *
     * @return the prompt text in the input field
     */
    public String getPromptText() {
        return input.getPromptText();
    }

    /**
     * Binds the input field to a setter with an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param setter   the setter to bind the input value to
     */
    public void bind(String errorMsg, Setter<String> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid())
                tryCatch(
                    errorMsg,
                    () -> setter.accept(getText())
                );
        });
    }

    /**
     * Binds the input field to a setter with an initial value and an error message for invalid input.
     *
     * @param errorMsg the error message to display if the input is invalid
     * @param value    the initial value to set in the input field
     * @param setter   the setter to bind the input value to
     */
    public void bind(String errorMsg, String value, Setter<String> setter) {
        bind(errorMsg, setter);
        setText(value);
    }
}
