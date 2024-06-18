package com.musigma.models;

import com.musigma.models.exception.ArtisteException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Artiste représente un artiste avec un nom, un genre musical et un prix.
 */
public class Artiste implements Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(Artiste.class);

    private String name;
    private String genre;
    private float price;
    private final ArrayList<Representation> representations;

    /**
     * Constructeur de la classe Artiste.
     *
     * @param name le nom de l'artiste
     * @param genre le genre musical de l'artiste
     * @param price le prix de l'artiste
     * @throws ArtisteException si les valeurs des paramètres ne sont pas valides
     */
    public Artiste(String name, String genre, float price) throws ArtisteException {
        LOGGER.info(String.format("Initialized Artiste 0x%x", super.hashCode()));
        representations = new ArrayList<>();
        setName(name);
        setGenre(genre);
        setPrice(price);
        LOGGER.info(String.format("Created Artiste 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Artiste.name 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Artiste.genre 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Artiste.price 0x%x", super.hashCode()));
    }

    /**
     * Ajoute une représentation à la liste des représentations de l'artiste.
     *
     * @param representation la représentation à ajouter
     * @throws ArtisteException si la représentation est null
     */
    public void addRepresentation(Representation representation) throws ArtisteException {
        if (representation == null)
            throw new ArtisteException("La représentation est null, doit être défini");
        representations.add(representation);
        LOGGER.info(String.format("Added Representation to Artiste.representations 0x%x", super.hashCode()));
    }

    /**
     * Supprime une représentation de la liste des représentations de l'artiste.
     *
     * @param representation la représentation à supprimer
     * @throws ArtisteException si la représentation n'a pas été trouvée
     */
    public void removeRepresentation(Representation representation) throws ArtisteException {
        if (!representations.remove(representation))
            throw new ArtisteException("La représentation n'a pas été trouvée");
        LOGGER.info(String.format("Removed Representation from Artiste.representations 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des représentations de l'artiste.
     *
     * @return la liste des représentations
     */
    public ArrayList<Representation> getRepresentations() {
        return representations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artiste artiste = (Artiste) o;
        return Float.compare(price, artiste.price) == 0 && Objects.equals(name, artiste.name) && Objects.equals(genre, artiste.genre) && Objects.equals(representations, artiste.representations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, genre, price, representations);
    }
}
