package com.musigma.controllers.components;

import com.musigma.utils.exceptionMethods.Setter;

import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Un composant JavaFX personnalisé qui étend RequiredTextField pour gérer les entrées de type numérique avec une validation supplémentaire.
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
    }

    /**
     * Constructeur qui charge le fichier FXML et configure le composant avec des paramètres de validation supplémentaires.
     *
     * @param positive Indique si le champ doit contenir une valeur positive.
     * @param notNull  Indique si le champ ne doit pas être null.
     */
    public NumberTextField(boolean positive, boolean notNull) {
        super();
        this.positive = positive;
        this.notNull = notNull;
    }

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

    /**
     * Récupère la valeur du champ d'entrée.
     *
     * @return La valeur du champ d'entrée en tant que T.
     */
    public abstract T getValue();

    /**
     * Définit la valeur du champ d'entrée.
     *
     * @param value La valeur à définir.
     */
    public abstract void setValue(T value);

    /**
     * Lie le champ d'entrée à un setter avec un message d'erreur pour une entrée invalide.
     *
     * @param errorMsg message d'erreur
     * @param setter   setter
     */
    public void bindValue(String errorMsg, Setter<T> setter) {
        input.setOnKeyTyped(e -> {
            if (isValid())
                tryCatch(
                        errorMsg,
                        () -> setter.accept(getValue())
                );
        });
    }

    /**
     * Lie le champ d'entrée à un setter avec un message d'erreur pour une entrée invalide.
     *
     * @param errorMsg Le message d'erreur à afficher si l'entrée est invalide.
     * @param value    La valeur à définir.
     * @param setter   Le setter pour lier la valeur d'entrée.
     */
    public void bindValue(String errorMsg, T value, Setter<T> setter) {
        bindValue(errorMsg, setter);
        setValue(value);
    }
}