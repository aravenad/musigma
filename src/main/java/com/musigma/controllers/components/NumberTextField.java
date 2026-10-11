package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;
import javafx.scene.control.TextFormatter;

import java.util.regex.Pattern;

import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Un composant JavaFX personnalisé qui étend RequiredTextField pour gérer les entrées de type numérique avec une validation supplémentaire.
 * <p>
 * Seules les saisies pouvant mener à un nombre sont acceptées, que le texte soit tapé, collé ou défini par le code.
 *
 * @param <T> Type de nombre pour le champ personnalisé.
 */
public abstract class NumberTextField<T extends Number> extends RequiredTextField {

    private boolean positive;
    private boolean notNull;

    /**
     * Constructeur qui charge le fichier FXML et configure le composant.
     */
    public NumberTextField() {
        super();
        Pattern partial = partialPattern();
        input.setTextFormatter(new TextFormatter<>(change ->
                partial.matcher(change.getControlNewText()).matches() ? change : null
        ));
    }

    /**
     * Constructeur qui charge le fichier FXML et configure le composant avec des paramètres de validation supplémentaires.
     *
     * @param positive Indique si le champ doit contenir une valeur positive.
     * @param notNull  Indique si le champ ne doit pas être null.
     */
    public NumberTextField(boolean positive, boolean notNull) {
        this();
        this.positive = positive;
        this.notNull = notNull;
    }

    /**
     * Retourne le motif des saisies acceptées pendant la frappe, y compris incomplètes (ex : « - » ou « 1. »).
     *
     * @return le motif des saisies acceptées
     */
    protected abstract Pattern partialPattern();

    /**
     * Convertit le texte saisi en nombre.
     *
     * @param text le texte saisi
     * @return le nombre correspondant
     * @throws NumberFormatException si le texte n'est pas un nombre valide
     */
    protected abstract T parse(String text);

    /**
     * Convertit un nombre en texte à afficher, sans notation scientifique.
     *
     * @param value le nombre
     * @return le texte à afficher
     */
    protected abstract String format(T value);

    /**
     * Indique si le champ ne doit pas être null.
     *
     * @return true si le champ ne doit pas être null, false sinon.
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Définit si le champ ne doit pas être null.
     *
     * @param notNull true si le champ ne doit pas être null, false sinon.
     */
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    /**
     * Indique si le champ doit contenir une valeur positive.
     *
     * @return true si le champ doit contenir une valeur positive, false sinon.
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * Définit si le champ doit contenir une valeur positive.
     *
     * @param positive true si le champ doit contenir une valeur positive, false sinon.
     */
    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    /**
     * Valide le champ d'entrée.
     *
     * @return true si le champ d'entrée est valide, false sinon.
     */
    @Override
    public boolean isValid() {
        if (!super.isValid())
            return false;
        double value;
        try {
            value = parse(getText()).doubleValue();
        } catch (NumberFormatException e) {
            setError("Nombre invalide");
            return false;
        }
        if (positive && value < 0) {
            setError("Valeur positive");
            return false;
        } else if (notNull && value == 0) {
            setError("Valeur non-nulle");
            return false;
        }
        return true;
    }

    /**
     * Récupère la valeur du champ d'entrée.
     *
     * @return La valeur du champ d'entrée en tant que T.
     * @throws NumberFormatException si le champ ne contient pas un nombre valide
     */
    public T getValue() {
        return parse(getText());
    }

    /**
     * Définit la valeur du champ d'entrée.
     *
     * @param value La valeur à définir.
     */
    public void setValue(T value) {
        setText(format(value));
    }

    /**
     * Lie le champ d'entrée à un setter : chaque modification valide du texte est transmise au setter.
     *
     * @param errorMsg message d'erreur
     * @param setter   setter
     */
    public void bindValue(String errorMsg, Setter<T> setter) {
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValid())
                tryCatch(
                        errorMsg,
                        () -> setter.accept(getValue())
                );
        });
    }

    /**
     * Affiche une valeur initiale puis lie le champ d'entrée à un setter.
     *
     * @param errorMsg Le message d'erreur à afficher si l'entrée est invalide.
     * @param value    La valeur initiale à afficher.
     * @param setter   Le setter pour lier la valeur d'entrée.
     */
    public void bindValue(String errorMsg, T value, Setter<T> setter) {
        setValue(value);
        bindValue(errorMsg, setter);
    }
}
