package com.musigma.models;

import com.musigma.models.exception.TypeTicketException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static fr.uga.iut2.musigma.util.Log.getLogger;

/**
 * La classe TypeTicket représente un type de ticket avec un identifiant unique,
 * un type, une quantité, un prix et une liste d'avantages.
 */
public class TypeTicket implements Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(TypeTicket.class);

    // Type du ticket (ex : VIP, Standard, etc.)
    private String type;

    // Quantité de ce type de ticket en vente
    private int quantity;

    // Prix unitaire du ticket
    private float price;

    // Liste des avantages associés à ce type de ticket
    private final ArrayList<Avantage> avantages;

    /**
     * Constructeur de la classe TypeTicket.
     *
     * @param type Type de ticket
     * @param quantity Quantité de tickets disponibles
     * @param price Prix unitaire du ticket
     */
    public TypeTicket(String type, int quantity, float price) throws TypeTicketException {
        LOGGER.info(String.format("Initialized TypeTicket 0x%x", super.hashCode()));
        avantages = new ArrayList<>();
        setType(type);
        setQuantity(quantity);
        setPrice(price);
        LOGGER.info(String.format("Created TypeTicket 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set TypeTicket.type 0x%x", super.hashCode()));
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
     * @throws TypeTicketException si la quantité n'est pas positive
     */
    public void setQuantity(int quantity) throws TypeTicketException {
        if (quantity < 0)
            throw new TypeTicketException("La quantité est négative, doit être positive");
        for (Avantage avantage : avantages) {
            Stock stock = avantage.getStock();
            if (stock.isFixed() && quantity * avantage.getQuantityByTicket() > stock.getQuantity())
                throw new TypeTicketException("La quantité de ticket multiplé par le nombre d'avantage par ticket surpasse le stock limité");
        }
        this.quantity = quantity;
        LOGGER.info(String.format("Set TypeTicket.quantity 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set TypeTicket.price 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des avantages associés à ce type de ticket.
     *
     * @return la liste des avantages
     */
    public ArrayList<Avantage> getAvantages() {
        return avantages;
    }

    /**
     * Ajoute un avantage à la liste des avantages de ce type de ticket.
     *
     * @param avantage l'avantage à ajouter
     * @throws TypeTicketException si l'avantage est null
     */
    public void addAvantage(Avantage avantage) throws TypeTicketException {
        if (avantage == null)
            throw new TypeTicketException("L'avantage est null, doit être défini");
        avantages.add(avantage);
        LOGGER.info(String.format("Added Avantage to TypeTicket.avantages 0x%x", super.hashCode()));
    }

    /**
     * Supprime un avantage de la liste des avantages de ce type de ticket.
     *
     * @param avantage l'avantage à supprimer
     * @throws TypeTicketException si l'avantage n'est pas trouvé dans la liste
     */
    public void removeAvantage(Avantage avantage) throws TypeTicketException {
        if (!avantages.remove(avantage))
            throw new TypeTicketException("L'avantage n'a pas été trouvé");
        LOGGER.info(String.format("Removed Avantage from TypeTicket.avantages 0x%x", super.hashCode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeTicket that = (TypeTicket) o;
        return quantity == that.quantity && Float.compare(price, that.price) == 0 && Objects.equals(type, that.type) && Objects.equals(avantages, that.avantages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, quantity, price, avantages);
    }
}
