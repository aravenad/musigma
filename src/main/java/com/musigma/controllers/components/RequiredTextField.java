package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static com.musigma.utils.Dialogs.tryCatch;

public class RequiredTextField extends GridPane {

    private static final String VIEW_PATH = "/com/musigma/views/components/error-text-field.fxml";

    @FXML protected TextField input;
    @FXML protected Label error;
    @FXML protected VBox errorBox;

    protected boolean isValid = false;

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

    @FXML
    private void initialize() {}

    public void requestFocus() {
        input.requestFocus();
    }

    public void setError(String error) {
        this.error.setText("* " + error);
        input.setStyle("-color-accent-emphasis: -color-danger-fg;-color-border-default: -color-danger-fg");
        errorBox.setVisible(true);
        isValid = false;
    }

    public void clearError() {
        if (isValid) return;
        input.setStyle("");
        errorBox.setVisible(false);
        isValid = true;
    }

    protected boolean isEmpty() {
        return isValid;
    }

    public boolean isValid() {
        if (input.getText().isEmpty()) {
            setError("Valeur requise");
            return false;
        } else {
            clearError();
            return true;
        }
    }

    public void setText(String text) {
        input.setText(text);
    }

    public String getText() {
        return input.getText();
    }

    public void setPromptText(String promptText) {
        input.setPromptText(promptText);
    }

    public String getPromptText() {
        return input.getPromptText();
    }

    public void bind(String errrorMsg, Setter<String> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid())
                tryCatch(
                    errrorMsg,
                    () -> setter.accept(getText())
                );
        });
    }

    public void bind(String errrorMsg, String value, Setter<String> setter) {
        bind(errrorMsg, setter);
        setText(value);
        isValid();
    }
}
