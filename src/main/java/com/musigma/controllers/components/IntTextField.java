package com.musigma.controllers.components;

/**
 * Un composant JavaFX personnalisé qui étend RequiredTextField pour gérer les entrées de type entier avec une validation supplémentaire.
 */
public class IntTextField extends NumberTextField<Integer> {

    /**
     * Constructeur qui initialise le composant et configure le champ d'entrée pour n'accepter que des valeurs entières.
     */
    public IntTextField() {
        super();
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\d)]*", ""));
            }
        });
    }

    /**
     * Constructeur qui initialise le composant avec des paramètres de validation supplémentaires.
     *
     * @param positive Indique si le champ doit contenir une valeur positive.
     * @param notNull Indique si le champ ne doit pas être null.
     */
    public IntTextField(boolean positive, boolean notNull) {
        super(positive, notNull);
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+-]?\\d+")) {
                input.setText(newValue.replaceAll("[^(+|\\-|\\d)]*", ""));
            }
        });
    }

    /**
     * Définit la valeur du champ d'entrée.
     *
     * @param value La valeur à définir.
     */
    @Override
    public void setValue(Integer value) {
        input.setText(Float.toString(value).replaceAll("^0*", ""));
        isValid();
    }

    /**
     * Récupère la valeur du champ d'entrée.
     *
     * @return La valeur du champ d'entrée en tant qu'Integer.
     */
    @Override
    public Integer getValue() {
        return Integer.parseInt(input.getText());
    }
}