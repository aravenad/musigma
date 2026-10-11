package com.musigma.models;

import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.TypeTicketException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe TypeTicket représente un type de ticket avec un identifiant unique,
 * un type, une quantité, un prix et une liste d'avantages.
 */
public final class TypeTicket implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Surface occupée par défaut par un détenteur de ticket (m²), selon la densité d'une fosse de concert.
     */
    public static final float SURFACE_PAR_DEFAUT = .42f;

    /**
     * Logger de la class
     */
    private static final Logger LOGGER = getLogger(TypeTicket.class);
    /**
     * Liste des avantages associés à ce type de ticket
     */
    private final ArrayList<Avantage> avantages;
    /**
     * Type de ticket
     */
    private String type;
    /**
     * Quantité de tickets disponibles
     */
    private int quantite;
    /**
     * Prix unitaire du ticket
     */
    private float prix;
    /**
     * Surface occupée par chaque détenteur de ce ticket (m²)
     */
    private float surface = SURFACE_PAR_DEFAUT;

    /**
     * Constructeur de la classe TypeTicket.
     *
     * @param type     Type de ticket
     * @param quantite Quantité de tickets disponibles
     * @param prix    Prix unitaire du ticket
     * @throws TypeTicketException si le type est null ou vide
     */
    public TypeTicket(String type, int quantite, float prix) throws TypeTicketException {
        LOGGER.fine("Initialized TypeTicket");
        avantages = new ArrayList<>();
        setType(type);
        setQuantite(quantite);
        setPrix(prix);
        LOGGER.fine("Created TypeTicket");
    }

    /**
     * Retourne le type du ticket.
     *
     * @return le type du ticket
     */
    public String getType() {
        return type;
    }

    /**
     * Définit le type du ticket.
     *
     * @param type le type du ticket
     * @throws TypeTicketException si le type est null ou vide
     */
    public void setType(String type) throws TypeTicketException {
        if (type == null || type.isBlank())
            throw new TypeTicketException("Le type est null ou vide, doit être défini");
        this.type = type;
        LOGGER.fine("Set TypeTicket.type");
    }

    /**
     * Retourne la quantité de tickets disponibles.
     *
     * @return la quantité de tickets disponibles
     */
    public int getQuantite() {
        return quantite;
    }

    /**
     * Définit la quantité de tickets disponibles.
     *
     * @param quantite la quantité de tickets disponibles
     * @throws TypeTicketException si la quantité n'est pas positive, ou si l'ensemble des tickets
     *                             consommant un stock limité dépasse ce stock
     */
    public void setQuantite(int quantite) throws TypeTicketException {
        if (quantite < 0)
            throw new TypeTicketException("La quantité est négative, doit être positive");
        for (Avantage avantage : avantages) {
            long delta = (long) (quantite - this.quantite) * avantage.getQuantiteParTicket();
            if (avantage.getStock().depasseLimite(delta))
                throw new TypeTicketException(String.format(
                        "Le stock limité \"%s\" ne suffit pas pour %d tickets \"%s\"",
                        avantage.getStock().getNom(), quantite, type
                ));
        }
        this.quantite = quantite;
        LOGGER.fine("Set TypeTicket.quantite");
    }

    /**
     * Retourne le prix unitaire du ticket.
     *
     * @return le prix unitaire du ticket
     */
    public float getPrix() {
        return prix;
    }

    /**
     * Définit le prix unitaire du ticket.
     *
     * @param prix le prix unitaire du ticket
     * @throws TypeTicketException si le prix n'est pas positif
     */
    public void setPrix(float prix) throws TypeTicketException {
        if (prix < 0)
            throw new TypeTicketException("Le prix est négatif, doit être positif");
        this.prix = prix;
        LOGGER.fine("Set TypeTicket.prix");
    }

    /**
     * Retourne la recette de ce type de ticket : la quantité multipliée par le prix.
     *
     * @return la recette en euros
     */
    public double getRecette() {
        return (double) quantite * prix;
    }

    /**
     * Retourne la surface occupée par chaque détenteur de ce ticket.
     *
     * @return la surface en m²
     */
    public float getSurface() {
        return surface;
    }

    /**
     * Définit la surface occupée par chaque détenteur de ce ticket (ex : 4 m² pour un VIP, 2 m² sinon).
     *
     * @param surface la surface en m²
     * @throws TypeTicketException si la surface n'est pas strictement positive
     */
    public void setSurface(float surface) throws TypeTicketException {
        if (!(surface > 0))
            throw new TypeTicketException("La surface par ticket est négative ou nulle, doit être positive");
        this.surface = surface;
        LOGGER.fine("Set TypeTicket.surface");
    }

    /**
     * Retourne la liste des avantages associés à ce type de ticket.
     *
     * @return la liste des avantages, non modifiable
     */
    public List<Avantage> getAvantages() {
        return Collections.unmodifiableList(avantages);
    }

    /**
     * Ajoute un avantage à ce type de ticket en le connectant aussi à son stock.
     * Sans effet si l'avantage est déjà connecté.
     *
     * @param avantage l'avantage à ajouter
     * @throws TypeTicketException si l'avantage est null ou concerne un autre type de ticket
     * @throws AvantageException   si l'avantage ne peut pas être connecté
     * @see Avantage#connecter()
     */
    public void ajouterAvantage(Avantage avantage) throws TypeTicketException, AvantageException {
        if (avantage == null)
            throw new TypeTicketException("L'avantage est null, doit être défini");
        if (avantage.getTypeTicket() != this)
            throw new TypeTicketException("L'avantage concerne un autre type de ticket");
        if (avantages.contains(avantage))
            return;
        avantage.connecter();
    }

    /**
     * Supprime un avantage de ce type de ticket et de son stock.
     *
     * @param avantage l'avantage à supprimer
     * @throws TypeTicketException si l'avantage n'est pas trouvé dans la liste
     * @see Avantage#deconnecter()
     */
    public void retirerAvantage(Avantage avantage) throws TypeTicketException {
        if (!avantages.contains(avantage))
            throw new TypeTicketException("L'avantage n'a pas été trouvé");
        avantage.detacher();
    }

    /**
     * Ajoute l'avantage à la liste, sans vérification. Réservé à {@link Avantage#connecter()}.
     *
     * @param avantage l'avantage à ajouter
     */
    void lier(Avantage avantage) {
        avantages.add(avantage);
        LOGGER.fine("Added Avantage to TypeTicket.avantages");
    }

    /**
     * Retire l'avantage de la liste, sans vérification. Réservé à {@link Avantage#detacher()}.
     *
     * @param avantage l'avantage à retirer
     */
    void delier(Avantage avantage) {
        avantages.remove(avantage);
        LOGGER.fine("Removed Avantage from TypeTicket.avantages");
    }

    @Override
    public String toString() {
        return type;
    }
}
