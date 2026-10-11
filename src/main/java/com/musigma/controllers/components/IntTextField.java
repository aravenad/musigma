package com.musigma.controllers.components;

import java.util.regex.Pattern;

/**
 * Un composant JavaFX personnalisé qui étend NumberTextField pour gérer les entrées de type entier.
 */
public class IntTextField extends NumberTextField<Integer> {

    /**
     * Saisies acceptées pendant la frappe : un signe optionnel suivi de chiffres.
     */
    private static final Pattern PARTIAL = Pattern.compile("[+-]?\\d*");

    /**
     * Constructeur qui initialise le composant pour n'accepter que des valeurs entières.
     */
    public IntTextField() {
        super();
    }

    /**
     * Constructeur qui initialise le composant avec des paramètres de validation supplémentaires.
     *
     * @param positive Indique si le champ doit contenir une valeur positive.
     * @param notNull  Indique si le champ ne doit pas être null.
     */
    public IntTextField(boolean positive, boolean notNull) {
        super(positive, notNull);
    }

    @Override
    protected Pattern partialPattern() {
        return PARTIAL;
    }

    @Override
    protected Integer parse(String text) {
        return Integer.parseInt(text);
    }

    @Override
    protected String format(Integer value) {
        return Integer.toString(value);
    }
}
