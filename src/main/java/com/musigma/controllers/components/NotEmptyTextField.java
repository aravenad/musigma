package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.beans.property.FloatProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static com.musigma.utils.Dialogs.tryCatch;

public class NotEmptyTextField extends Pane {

    private static final String VIEW_PATH = "/com/musigma/views/components/error-text-field.fxml";

    @FXML protected TextField input;
    @FXML protected Text error;
    @FXML protected VBox errorBox;
    @FXML protected Label label;

    protected boolean isValid = false;

    public NotEmptyTextField() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_PATH));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(NotEmptyTextField.this);
        tryCatch(
            String.format("Chargement du composant %s impossible", getClass().getSimpleName()),
            fxmlLoader::load
        );
    }

    protected void setError(String error) {
        this.error.setText("* " + error);
        input.getSkin().getNode().setStyle("-color-accent-emphasis: -color-danger-fg;-color-border-default: -color-danger-fg");
        errorBox.setVisible(true);
        isValid = false;
    }

    protected void unSetError() {
        if (isValid) return;
        Skin skin = input.getSkin();
        if (skin != null)
            skin.getNode().setStyle("");
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
            unSetError();
            return true;
        }
    }

    public void setLabel(String promptText) {
        label.setText(promptText);
    }

    public String getLabel() {
        return label.getText();
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
                    () -> setter.accept(input.getText())
                );
        });
    }

    public void bind(String errrorMsg, String value, Setter<String> setter) {
        bind(errrorMsg, setter);
        input.setText(value);
        isValid();
    }
}
