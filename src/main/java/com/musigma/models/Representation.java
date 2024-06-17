package com.musigma.models;

import com.musigma.models.exception.RepresentationException;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;

import static com.musigma.util.Log.getLogger;

/**
 * La classe Representation représente une représentation d'un artiste lors d'un festival,
 * avec une date de début, une durée, une scène et un artiste associé.
 */
public class Representation implements Comparable<Representation>, Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(Representation.class);

    // Delta de début par rapport au festival en minute
    private int startDelta;

    // Durée en minute
    private int duration;

    // Scene attribuée à la représentation
    private String scene;

    // Artiste se représentant
    private Artiste artiste;

    /**
     * Constructeur de la classe Representation.
     *
     * @param startDelta la différence par rapport au début de la représentation en minutes
     * @param duration la durée de la représentation en minutes
     * @param scene la scène de la représentation
     * @param artiste l'artiste associé à la représentation
     */
    public Representation(int startDelta, int duration, String scene, Artiste artiste) {
        LOGGER.info(String.format("Initialized Representation 0x%x", super.hashCode()));
        this.startDelta = startDelta;
        this.duration = duration;
        this.scene = scene;
        this.artiste = artiste;
        LOGGER.info(String.format("Created Representation 0x%x", super.hashCode()));
    }

    /**
     * Retourne l'artiste associé à la représentation.
     *
     * @return l'artiste de la représentation
     */
    public Artiste getArtiste() {
        return artiste;
    }

    /**
     * Défini l'artiste associé à la représentation.
     *
     * @param artiste l'artiste de la représentation
     * @throws RepresentationException si l'artiste est null
     */
    public void setArtiste(Artiste artiste) throws RepresentationException {
        if (artiste == null)
            throw new RepresentationException("Artiste null, doit être défini");
        this.artiste = artiste;
        LOGGER.info(String.format("Set Representation.artiste 0x%x", super.hashCode()));
    }

    /**
     * Retourne la différence par rapport au début de la représentation en minutes.
     *
     * @return la différence par rapport au début de la représentation en minutes
     */
    public int getStartDelta() {
        return startDelta;
    }

    /**
     * Défini la différence par rapport au début de la représentation en minutes.
     *
     * @param startDelta la différence de la date de début de la représentation avec la date de début du festival en minute
     * @throws RepresentationException si la différence est négative
     */
    public void setStartDelta(int startDelta) throws RepresentationException {
        if (startDelta < 0)
            throw new RepresentationException("La différence avec la date de début du festival est négative, doit être positif");
        this.startDelta = startDelta;
        LOGGER.info(String.format("Set Representation.startDelta 0x%x", super.hashCode()));
    }

    /**
     * Retourne la durée de la représentation.
     *
     * @return la durée de la représentation
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Défini la durée de la représentation.
     *
     * @param duration la durée de la représentation
     */
    public void setDuration(int duration) throws RepresentationException {
        if (duration <= 0)
            throw new RepresentationException("La durée est négative ou nulle, doit être positif et non nulle");
        this.duration = duration;
        LOGGER.info(String.format("Set Representation.duration 0x%x", super.hashCode()));
    }

    /**
     * Retourne la scène de la représentation.
     *
     * @return la scène de la représentation
     */
    public String getScene() {
        return scene;
    }

    /**
     * Défini la scène de la représentation.
     *
     * @param scene la scène de la représentation
     */
    public void setScene(String scene) throws RepresentationException {
        if (scene == null || scene.isBlank())
            throw new RepresentationException("La scene est null ou vide, doit être définie");
        this.scene = scene;
        LOGGER.info(String.format("Set Representation.scene 0x%x", super.hashCode()));
    }

    /**
     * Vérifie s'il y a collision entre cette représentation et une autre représentation donnée,
     * en considérant qu'elles ont lieu sur la même scène.
     *
     * @param representation l'autre représentation à comparer
     * @return true s'il y a collision, false sinon
     */
    public boolean isColliding(Representation representation) {
        // Exemple :
        // Soit a et b sur la même scène, sachant l'emploi du temps suivant:
        // a       a + a_d
        // |------->|
        //       b        b + b_d
        //       |--------->|
        // Alors il y a collision car a < b & b < a + a_d
        if (!scene.equals(representation.getScene()))
            return false;
        return startDelta < representation.getStartDelta() ? (representation.getStartDelta() < startDelta + duration) : ( startDelta < representation.getStartDelta() + representation.getDuration());
    }

    /**
     * Compare cette représentation à une autre représentation par date de début.
     *
     * @param representation la représentation à comparer
     * @return un nombre négatif, zéro ou un nombre positif si cette représentation est antérieure, égale ou postérieure à la représentation spécifiée
     */
    @Override
    public int compareTo(Representation representation) {
        return startDelta - representation.getStartDelta();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Representation that = (Representation) o;
        return startDelta == that.startDelta && duration == that.duration && Objects.equals(scene, that.scene) && Objects.equals(artiste, that.artiste);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDelta, duration, scene, artiste);
    }
}
