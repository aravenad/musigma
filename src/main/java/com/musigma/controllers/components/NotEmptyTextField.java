package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
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
        this.error.setVisible(true);
        errorBox.setStyle("-fx-background-color: -color-bg-default");
    }

    protected void unSetError() {
        this.error.setVisible(false);
        errorBox.setStyle("");
    }

    protected boolean checkInput() {
        if (input.getText().isEmpty()){
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
            if (checkInput())
                tryCatch(
                    errrorMsg,
                    () -> setter.accept(input.getText())
                );
        });
    }

    public void bind(String errrorMsg, String value, Setter<String> setter) {
        input.setText(value);
        checkInput();
        bind(errrorMsg, setter);
    }
}
