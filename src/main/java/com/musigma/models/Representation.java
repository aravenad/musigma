package com.musigma.models;

import com.musigma.models.exception.RepresentationException;

import java.io.Serializable;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Representation représente une représentation d'un artiste lors d'un festival,
 * avec une date de début, une durée, une scène et un artiste associé.
 * <p>
 * Une représentation planifiée ne se modifie qu'au travers de {@link Festival#updateRepresentation},
 * qui garantit l'absence de chevauchement sur une même scène.
 */
public final class Representation implements Comparable<Representation>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger pour afficher les logs.
     */
    private static final Logger LOGGER = getLogger(Representation.class);

    /**
     * Delta de début par rapport au festival en minute
     */
    private int startDelta;

    /**
     * Durée de la représentation en minute
     */
    private int duration;

    /**
     * Scène de la représentation
     */
    private String scene;

    /**
     * Artiste associé à la représentation
     */
    private Artiste artiste;

    /**
     * Constructeur de la classe Representation.
     *
     * @param startDelta la différence par rapport au début de la représentation en minutes
     * @param duration   la durée de la représentation en minutes
     * @param scene      la scène de la représentation
     * @param artiste    l'artiste associé à la représentation
     * @throws RepresentationException si les valeurs des paramètres ne sont pas valides
     */
    public Representation(int startDelta, int duration, String scene, Artiste artiste) throws RepresentationException {
        LOGGER.fine("Initialized Representation");
        setStartDelta(startDelta);
        setDuration(duration);
        setScene(scene);
        setArtiste(artiste);
        LOGGER.fine("Created Representation");
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
    void setArtiste(Artiste artiste) throws RepresentationException {
        if (artiste == null)
            throw new RepresentationException("Artiste null, doit être défini");
        this.artiste = artiste;
        LOGGER.fine("Set Representation.artiste");
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
    void setStartDelta(int startDelta) throws RepresentationException {
        if (startDelta < 0)
            throw new RepresentationException("La différence avec la date de début du festival est négative, doit être positif");
        this.startDelta = startDelta;
        LOGGER.fine("Set Representation.startDelta");
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
     * @throws RepresentationException si la durée est négative ou nulle
     */
    void setDuration(int duration) throws RepresentationException {
        if (duration <= 0)
            throw new RepresentationException("La durée est négative ou nulle, doit être positif et non nulle");
        this.duration = duration;
        LOGGER.fine("Set Representation.duration");
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
     * @throws RepresentationException si la scène est null ou vide
     */
    void setScene(String scene) throws RepresentationException {
        if (scene == null || scene.isBlank())
            throw new RepresentationException("La scene est null ou vide, doit être définie");
        this.scene = scene;
        LOGGER.fine("Set Representation.scene");
    }

    /**
     * Vérifie s'il y a collision entre cette représentation et une autre représentation donnée,
     * en considérant qu'elles ont lieu sur la même scène.
     *
     * @param representation l'autre représentation à comparer
     * @return true s'il y a collision, false sinon
     */
    public boolean isColliding(Representation representation) {
        return isColliding(representation.getScene(), representation.getStartDelta(), representation.getDuration());
    }

    /**
     * Vérifie si cette représentation occupe la scène donnée pendant le créneau donné.
     *
     * @param scene      la scène du créneau
     * @param startDelta le début du créneau en minutes depuis le début du festival
     * @param duration   la durée du créneau en minutes
     * @return true si le créneau chevauche cette représentation, false sinon
     */
    public boolean isColliding(String scene, int startDelta, int duration) {
        // Exemple :
        // Soit a et b sur la même scène, sachant l'emploi du temps suivant:
        // a       a + a_d
        // |------->|
        //       b        b + b_d
        //       |--------->|
        // Alors il y a collision car a < b + b_d & b < a + a_d
        if (!this.scene.equals(scene))
            return false;
        return this.startDelta < startDelta + duration && startDelta < this.startDelta + this.duration;
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
    public String toString() {
        return String.format("%s sur %s (+%d min, %d min)", artiste.getName(), scene, startDelta, duration);
    }
}
