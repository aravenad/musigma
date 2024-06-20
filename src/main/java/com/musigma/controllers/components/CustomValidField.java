package com.musigma.controllers.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class CustomValidField <T extends Region> {
    public Label label;
    public T node;

    public CustomValidField(String label, T node) {
        this.label = new Label(label + " :");
        this.node = node;
    }

    public boolean isValid() {
        if (RequiredTextField.class.isAssignableFrom(node.getClass()))
            return ((RequiredTextField) node).isValid();
        return true;
    }
}
