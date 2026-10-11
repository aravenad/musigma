package com.musigma.controllers.components;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Un composant JavaFX personnalisé qui étend NumberTextField pour gérer les entrées de type float.
 * La virgule est acceptée comme séparateur décimal, au même titre que le point.
 */
public class FloatTextField extends NumberTextField<Float> {

    /**
     * Saisies acceptées pendant la frappe : un signe optionnel, des chiffres, puis une partie décimale optionnelle.
     */
    private static final Pattern PARTIAL = Pattern.compile("[+-]?\\d*([.,]\\d*)?");

    /**
     * Constructeur qui initialise le composant pour n'accepter que des valeurs décimales.
     */
    public FloatTextField() {
        super();
    }

    /**
     * Constructeur qui initialise le composant avec des paramètres de validation supplémentaires.
     *
     * @param positive Indique si le champ doit contenir une valeur positive.
     * @param notNull  Indique si le champ ne doit pas être null.
     */
    public FloatTextField(boolean positive, boolean notNull) {
        super(positive, notNull);
    }

    @Override
    protected Pattern partialPattern() {
        return PARTIAL;
    }

    @Override
    protected Float parse(String text) {
        String normalized = text.replace(',', '.');
        // Float.parseFloat accepte « 1. » et « .5 », mais pas un séparateur seul
        if (!normalized.matches("[+-]?(\\d+\\.?\\d*|\\.\\d+)"))
            throw new NumberFormatException("Nombre invalide : " + text);
        float value = Float.parseFloat(normalized);
        if (Float.isInfinite(value))
            throw new NumberFormatException("Nombre trop grand : " + text);
        return value;
    }

    @Override
    protected String format(Float value) {
        // Float.toString passe en notation scientifique à partir de 10^7 (ex : 1.2E7)
        return new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }
}
