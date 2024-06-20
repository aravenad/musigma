package com.musigma.controllers.components;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * Classe CustomValidField qui représente un champ personnalisé avec validation.
 *
 * @param <T> Type de région pour le champ personnalisé.
 */
public class CustomValidField<T extends Region> {
    /**
     * Label du champ.
     */
    public Label label;
    /**
     * Noeud de type T pour le champ.
     */
    public T node;

    /**
     * Constructeur de CustomValidField.
     *
     * @param label Le label du champ.
     * @param node  Le noeud de type T pour le champ.
     */
    public CustomValidField(String label, T node) {
        this.label = new Label(label + " :");
        this.node = node;
    }

    /**
     * Vérifie si le champ est valide.
     *
     * @return true si le champ est valide, false sinon.
     */
    public boolean isValid() {
        if (RequiredTextField.class.isAssignableFrom(node.getClass()))
            return ((RequiredTextField) node).isValid();
        return true;
    }
}