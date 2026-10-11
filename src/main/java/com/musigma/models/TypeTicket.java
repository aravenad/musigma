package com.musigma.models;

import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.TypeTicketException;

import java.io.IOException;
import java.io.ObjectInputStream;
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
public class TypeTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Surface occupée par défaut par un détenteur de ticket (m²), selon la densité d'une fosse de concert.
     */
    public static final float DEFAULT_SURFACE = .42f;

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
    private int quantity;
    /**
     * Prix unitaire du ticket
     */
    private float price;
    /**
     * Surface occupée par chaque détenteur de ce ticket (m²)
     */
    private float surface = DEFAULT_SURFACE;

    /**
     * Constructeur de la classe TypeTicket.
     *
     * @param type     Type de ticket
     * @param quantity Quantité de tickets disponibles
     * @param price    Prix unitaire du ticket
     * @throws TypeTicketException si le type est null ou vide
     */
    public TypeTicket(String type, int quantity, float price) throws TypeTicketException {
        LOGGER.info("Initialized TypeTicket");
        avantages = new ArrayList<>();
        setType(type);
        setQuantity(quantity);
        setPrice(price);
        LOGGER.info("Created TypeTicket");
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
        LOGGER.info("Set TypeTicket.type");
    }

    /**
     * Retourne la quantité de tickets disponibles.
     *
     * @return la quantité de tickets disponibles
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Définit la quantité de tickets disponibles.
     *
     * @param quantity la quantité de tickets disponibles
     * @throws TypeTicketException si la quantité n'est pas positive, ou si l'ensemble des tickets
     *                             consommant un stock limité dépasse ce stock
     */
    public void setQuantity(int quantity) throws TypeTicketException {
        if (quantity < 0)
            throw new TypeTicketException("La quantité est négative, doit être positive");
        for (Avantage avantage : avantages) {
            long delta = (long) (quantity - this.quantity) * avantage.getQuantityByTicket();
            if (avantage.getStock().exceedsLimit(delta))
                throw new TypeTicketException(String.format(
                        "Le stock limité \"%s\" ne suffit pas pour %d tickets \"%s\"",
                        avantage.getStock().getName(), quantity, type
                ));
        }
        this.quantity = quantity;
        LOGGER.info("Set TypeTicket.quantity");
    }

    /**
     * Retourne le prix unitaire du ticket.
     *
     * @return le prix unitaire du ticket
     */
    public float getPrice() {
        return price;
    }

    /**
     * Définit le prix unitaire du ticket.
     *
     * @param price le prix unitaire du ticket
     * @throws TypeTicketException si le prix n'est pas positif
     */
    public void setPrice(float price) throws TypeTicketException {
        if (price < 0)
            throw new TypeTicketException("Le prix est négatif, doit être positif");
        this.price = price;
        LOGGER.info("Set TypeTicket.price");
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
        LOGGER.info("Set TypeTicket.surface");
    }

    /**
     * Complète un type de ticket enregistré avant l'ajout de la surface par ticket.
     *
     * @param in le flux de lecture
     * @throws IOException            si la lecture échoue
     * @throws ClassNotFoundException si une classe du fichier est inconnue
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (surface <= 0)
            surface = DEFAULT_SURFACE;
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
     * @see Avantage#connect()
     */
    public void addAvantage(Avantage avantage) throws TypeTicketException, AvantageException {
        if (avantage == null)
            throw new TypeTicketException("L'avantage est null, doit être défini");
        if (avantage.getTicketType() != this)
            throw new TypeTicketException("L'avantage concerne un autre type de ticket");
        if (avantages.contains(avantage))
            return;
        avantage.connect();
    }

    /**
     * Supprime un avantage de ce type de ticket et de son stock.
     *
     * @param avantage l'avantage à supprimer
     * @throws TypeTicketException si l'avantage n'est pas trouvé dans la liste
     * @see Avantage#disconnect()
     */
    public void removeAvantage(Avantage avantage) throws TypeTicketException {
        if (!avantages.contains(avantage))
            throw new TypeTicketException("L'avantage n'a pas été trouvé");
        avantage.detach();
    }

    /**
     * Ajoute l'avantage à la liste, sans vérification. Réservé à {@link Avantage#connect()}.
     *
     * @param avantage l'avantage à ajouter
     */
    void link(Avantage avantage) {
        avantages.add(avantage);
        LOGGER.info("Added Avantage to TypeTicket.avantages");
    }

    /**
     * Retire l'avantage de la liste, sans vérification. Réservé à {@link Avantage#detach()}.
     *
     * @param avantage l'avantage à retirer
     */
    void unlink(Avantage avantage) {
        avantages.remove(avantage);
        LOGGER.info("Removed Avantage from TypeTicket.avantages");
    }

    @Override
    public String toString() {
        return type;
    }
}
