package com.musigma.models;

import com.musigma.models.exception.ArtisteException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Artiste représente un artiste avec un nom, un genre musical et un prix.
 */
public final class Artiste implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger de la classe Artiste.
     */
    private static final Logger LOGGER = getLogger(Artiste.class);
    /**
     * Liste des représentations de l'artiste.
     */
    private final ArrayList<Representation> representations;
    /**
     * Nom de l'artiste.
     */
    private String name;
    /**
     * Genre musical de l'artiste.
     */
    private String genre;
    /**
     * Prix de l'artiste.
     */
    private float price;

    /**
     * Constructeur de la classe Artiste.
     *
     * @param name  le nom de l'artiste
     * @param genre le genre musical de l'artiste
     * @param price le prix de l'artiste
     * @throws ArtisteException si les valeurs des paramètres ne sont pas valides
     */
    public Artiste(String name, String genre, float price) throws ArtisteException {
        LOGGER.fine("Initialized Artiste");
        representations = new ArrayList<>();
        setName(name);
        setGenre(genre);
        setPrice(price);
        LOGGER.fine("Created Artiste");
    }

    /**
     * Retourne le nom de l'artiste.
     *
     * @return le nom de l'artiste
     */
    public String getName() {
        return name;
    }

    /**
     * Défini le nom de l'artiste.
     *
     * @param name le nom de l'artiste
     * @throws ArtisteException si le nom est null ou vide
     */
    public void setName(String name) throws ArtisteException {
        if (name == null || name.isBlank())
            throw new ArtisteException("Le nom est null ou vide, doit être défini");
        this.name = name;
        LOGGER.fine("Set Artiste.name");
    }

    /**
     * Retourne le genre musical de l'artiste.
     *
     * @return le genre musical de l'artiste
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Défini le genre musical de l'artiste.
     *
     * @param genre le genre musical de l'artiste
     * @throws ArtisteException si le genre est null ou vide
     */
    public void setGenre(String genre) throws ArtisteException {
        if (genre == null || genre.isBlank())
            throw new ArtisteException("Le genre est null ou vide, doit être défini");
        this.genre = genre;
        LOGGER.fine("Set Artiste.genre");
    }

    /**
     * Retourne le prix de l'artiste.
     *
     * @return le prix de l'artiste
     */
    public float getPrice() {
        return price;
    }

    /**
     * Défini le prix de l'artiste.
     *
     * @param price le prix de l'artiste
     * @throws ArtisteException si le prix est négatif
     */
    public void setPrice(float price) throws ArtisteException {
        if (price < 0)
            throw new ArtisteException("Le prix est négatif, doit être positif");
        this.price = price;
        LOGGER.fine("Set Artiste.price");
    }

    /**
     * Ajoute une représentation à la liste des représentations de l'artiste.
     * Appelée par {@link Festival#addRepresentation} lors de la planification.
     *
     * @param representation la représentation à ajouter
     * @throws ArtisteException si la représentation est null ou concerne un autre artiste
     */
    void addRepresentation(Representation representation) throws ArtisteException {
        if (representation == null)
            throw new ArtisteException("La représentation est null, doit être défini");
        if (representation.getArtiste() != this)
            throw new ArtisteException("La représentation concerne un autre artiste");
        representations.add(representation);
        LOGGER.fine("Added Representation to Artiste.representations");
    }

    /**
     * Supprime une représentation de la liste des représentations de l'artiste.
     * Appelée par {@link Festival#removeRepresentation}.
     *
     * @param representation la représentation à supprimer
     * @throws ArtisteException si la représentation n'a pas été trouvée
     */
    void removeRepresentation(Representation representation) throws ArtisteException {
        if (!representations.remove(representation))
            throw new ArtisteException("La représentation n'a pas été trouvée");
        LOGGER.fine("Removed Representation from Artiste.representations");
    }

    /**
     * Retourne la liste des représentations de l'artiste.
     *
     * @return la liste des représentations, non modifiable
     */
    public List<Representation> getRepresentations() {
        return Collections.unmodifiableList(representations);
    }

    @Override
    public String toString() {
        return name;
    }
}
