package com.musigma.controllers;

import com.musigma.models.Festival;
import com.musigma.models.exception.TypeTicketException;
import javafx.scene.Node;

/**
 * La classe abstraite WorkspaceController est la base pour les contrôleurs qui
 * manipulent les espaces de travail dans l'application. Elle fait office
 * de base a chaque workspace afin de structurer l'application.
 */
public abstract class WorkspaceController {

    /**
     * Le festival à manipuler.
     */
    protected Festival festival;

    /**
     * Initialise le contrôleur avec le festival spécifié.
     *
     * @param festival le festival à initialiser
     */
    public void initialize(Festival festival) {
        this.festival = festival;
    }

    /**
     * La classe interne WorkspaceRegister représente un lien vers un espace de travail
     * avec un nom, un chemin d'icône, un chemin de vue et un bouton d'ouverture.
     */
    public static class WorkspaceRegister {
        /**
         * Le nom de l'espace de travail.
         */
        public String name;
        /**
         * Le chemin de l'icône de l'espace de travail.
         */
        public String iconPath;
        /**
         * Le chemin de la vue de l'espace de travail.
         */
        public String viewPath;
        /**
         * Le bouton d'ouverture de l'espace de travail.
         */
        public Node openButton;

        /**
         * Constructeur de la classe WorkspaceRegister.
         *
         * @param name     le nom de l'espace de travail
         * @param iconPath le chemin de l'icône de l'espace de travail
         * @param viewPath le chemin de la vue de l'espace de travail
         */
        public WorkspaceRegister(String name, String iconPath, String viewPath) {
            this.name = name;
            this.iconPath = iconPath;
            this.viewPath = viewPath;
        }
    }
}
