package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Un composant JavaFX personnalisé représentant un champ de texte requis avec gestion des erreurs.
 */
public class RequiredTextField extends GridPane {
    /**
     * Chemin de la vue FXML du composant.
     */
    private static final String VIEW_PATH = "/com/musigma/views/components/error-text-field.fxml";
    /**
     * Champ de texte pour l'entrée de l'utilisateur.
     */
    @FXML
    protected TextField input;
    /**
     * Label d'erreur pour afficher les messages d'erreur.
     */
    @FXML
    protected Label error;
    /**
     * Boîte d'erreur pour afficher le message d'erreur.
     */
    @FXML
    protected VBox errorBox;
    /**
     * Indique si le champ d'entrée est valide.
     */
    protected boolean isValid = false;

    /**
     * Constructeur qui charge le fichier FXML et configure le composant.
     */
    public RequiredTextField() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(VIEW_PATH));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(RequiredTextField.this);
        tryCatch(
                String.format("Impossible de charger le composant %s", getClass().getSimpleName()),
                fxmlLoader::load
        );
        input.setOnKeyTyped(e -> isValid());
        isValid();
    }

    /**
     * Demande le focus sur le champ d'entrée.
     */
    public void requestFocus() {
        input.requestFocus();
    }

    /**
     * Définit un message d'erreur et applique un style d'erreur.
     *
     * @param error le message d'erreur à afficher
     */
    public void setError(String error) {
        this.error.setText("* " + error);
        input.setStyle("-color-accent-emphasis: -color-danger-fg;-color-border-default: -color-danger-fg");
        errorBox.setVisible(true);
        isValid = false;
    }

    /**
     * Efface le message d'erreur et supprime le style d'erreur.
     */
    public void clearError() {
        if (isValid) return;
        input.setStyle("");
        errorBox.setVisible(false);
        isValid = true;
    }

    /**
     * Vérifie si le champ d'entrée est vide.
     *
     * @return true si le champ d'entrée est vide, false sinon
     */
    protected boolean isEmpty() {
        return isValid;
    }

    /**
     * Valide le champ d'entrée.
     *
     * @return true si le champ d'entrée est valide, false sinon
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
     * Définit le texte du champ d'entrée.
     *
     * @param text le texte à définir
     */
    public void setText(String text) {
        input.setText(text);
        isValid();
    }

    /**
     * Obtient le texte du champ d'entrée.
     *
     * @return le texte dans le champ d'entrée
     */
    public String getText() {
        return input.getText();
    }

    /**
     * Définit le texte d'invite du champ d'entrée.
     *
     * @param promptText le texte d'invite à définir
     */
    public void setPromptText(String promptText) {
        input.setPromptText(promptText);
    }

    /**
     * Obtient le texte d'invite du champ d'entrée.
     *
     * @return le texte d'invite dans le champ d'entrée
     */
    public String getPromptText() {
        return input.getPromptText();
    }

    /**
     * Lie le champ d'entrée à un setter avec un message d'erreur pour une entrée invalide.
     *
     * @param errorMsg le message d'erreur à afficher si l'entrée est invalide
     * @param setter   le setter pour lier la valeur d'entrée
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
     * Lie le champ d'entrée à un setter avec une valeur initiale et un message d'erreur pour une entrée invalide.
     *
     * @param errorMsg le message d'erreur à afficher si l'entrée est invalide
     * @param value    la valeur initiale à définir dans le champ d'entrée
     * @param setter   le setter pour lier la valeur d'entrée
     */
    public void bind(String errorMsg, String value, Setter<String> setter) {
        bind(errorMsg, setter);
        setText(value);
    }
}