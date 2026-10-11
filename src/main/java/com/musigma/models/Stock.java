package com.musigma.models;

import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.StockException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Stock représente un stock avec un nom, une quantité absolue,
 * et une liste d'avantages.
 */
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger pour afficher les logs.
     */
    private static final Logger LOGGER = getLogger(Stock.class);
    /**
     * Liste des avantages associés à ce stock.
     */
    private final ArrayList<Avantage> avantages;
    /**
     * Nom du stock.
     */
    private String name;
    /**
     * Quantité du stock.
     */
    private int quantity;
    /**
     * Si la quantité du stock est une limite que les tickets ne peuvent pas dépasser.
     */
    private boolean fixed;
    /**
     * Prix du stock.
     */
    private double prix;

    /**
     * Constructeur de la classe Stock.
     *
     * @param name     Nom du stock
     * @param quantity Quantité absolue du stock
     * @param fixed    Si la quantité du stock est une limite pour les tickets
     * @param prix     Prix du stock
     * @throws StockException si le nom est null ou vide, si la quantité est négative ou nulle (0)
     */
    public Stock(String name, int quantity, boolean fixed, double prix) throws StockException {
        LOGGER.info("Initialized Stock");
        avantages = new ArrayList<>();
        setName(name);
        setQuantity(quantity);
        setFixed(fixed);
        setPrix(prix);
        LOGGER.info("Created Stock");
    }

    /**
     * Retourne le nom du stock.
     *
     * @return le nom du stock
     */
    public String getName() {
        return name;
    }

    /**
     * Définit le nom du stock.
     *
     * @param name le nom du stock
     * @throws StockException si le nom est null ou vide
     */
    public void setName(String name) throws StockException {
        if (name == null || name.isBlank())
            throw new StockException("Le nom est null ou vide, doit être défini");
        this.name = name;
        LOGGER.info("Set Stock.name");
    }

    /**
     * Retourne si la quantité du stock est une limite pour les tickets.
     *
     * @return true si le stock est limité, false sinon
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Définit si la quantité du stock est une limite pour les tickets.
     *
     * @param fixed true pour limiter les tickets à la quantité du stock, false sinon
     * @throws StockException si les tickets consomment déjà plus que la quantité du stock
     */
    public void setFixed(boolean fixed) throws StockException {
        if (fixed && getConsumedQuantity() > quantity)
            throw new StockException(String.format(
                    "Les tickets consomment déjà %d unités, le stock de %d ne peut pas être limité",
                    getConsumedQuantity(), quantity
            ));
        this.fixed = fixed;
        LOGGER.info("Set Stock.fixed");
    }

    /**
     * Retourne la quantité absolue du stock.
     *
     * @return la quantité absolue du stock
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Définit la quantité absolue du stock.
     *
     * @param quantity la quantité absolue du stock
     * @throws StockException si la quantité est négative ou nulle (0),
     *                        ou inférieure à ce que consomment les tickets d'un stock limité
     */
    public void setQuantity(int quantity) throws StockException {
        if (quantity <= 0) {
            throw new StockException("La quantité absolue est négative ou nulle (0), doit être défini");
        }
        if (fixed && getConsumedQuantity() > quantity) {
            throw new StockException(String.format(
                    "Les tickets consomment %d unités, le stock limité ne peut pas descendre à %d",
                    getConsumedQuantity(), quantity
            ));
        }
        this.quantity = quantity;
        LOGGER.info("Set Stock.quantity");
    }

    /**
     * Retourne le prix du stock.
     *
     * @return le prix du stock
     */
    public double getPrix() {
        return prix;
    }

    /**
     * Définit le prix du stock.
     *
     * @param prix le prix du stock
     * @throws StockException si le prix est négatif ou nul (0)
     */
    public void setPrix(double prix) throws StockException {
        if (prix <= 0) {
            throw new StockException("Le prix est négatif ou nul (0), doit être défini");
        }
        this.prix = prix;
        LOGGER.info("Set Stock.prix");
    }

    /**
     * Retourne la liste des avantages associés à ce stock.
     *
     * @return la liste des avantages, non modifiable
     */
    public List<Avantage> getAvantages() {
        return Collections.unmodifiableList(avantages);
    }

    /**
     * Retourne la quantité consommée par les tickets : pour chaque avantage,
     * le nombre de tickets multiplié par la quantité par ticket.
     *
     * @return la quantité consommée
     */
    public long getConsumedQuantity() {
        return avantages.stream()
                .mapToLong(avantage -> avantage.contribution(avantage.getQuantityByTicket()))
                .sum();
    }

    /**
     * Indique si une consommation supplémentaire dépasserait la quantité d'un stock limité.
     *
     * @param additionalConsumption la consommation ajoutée (négative si elle diminue)
     * @return true si le stock est limité et serait dépassé, false sinon
     */
    boolean exceedsLimit(long additionalConsumption) {
        return fixed && additionalConsumption > 0 && getConsumedQuantity() + additionalConsumption > quantity;
    }

    /**
     * Ajoute un avantage à ce stock en le connectant aussi à son type de ticket.
     * Sans effet si l'avantage est déjà connecté.
     *
     * @param avantage l'avantage à ajouter
     * @throws StockException    si l'avantage est null ou concerne un autre stock
     * @throws AvantageException si l'avantage ne peut pas être connecté
     * @see Avantage#connect()
     */
    public void addAvantage(Avantage avantage) throws StockException, AvantageException {
        if (avantage == null)
            throw new StockException("L'avantage est null, doit être défini");
        if (avantage.getStock() != this)
            throw new StockException("L'avantage concerne un autre stock");
        if (avantages.contains(avantage))
            return;
        avantage.connect();
    }

    /**
     * Supprime un avantage de ce stock et de son type de ticket.
     *
     * @param avantage l'avantage à supprimer
     * @throws StockException si l'avantage n'est pas trouvé dans la liste
     * @see Avantage#disconnect()
     */
    public void removeAvantage(Avantage avantage) throws StockException {
        if (!avantages.contains(avantage))
            throw new StockException("L'avantage n'a pas été trouvé");
        avantage.detach();
    }

    /**
     * Ajoute l'avantage à la liste, sans vérification. Réservé à {@link Avantage#connect()}.
     *
     * @param avantage l'avantage à ajouter
     */
    void link(Avantage avantage) {
        avantages.add(avantage);
        LOGGER.info("Added Avantage to Stock.avantages");
    }

    /**
     * Retire l'avantage de la liste, sans vérification. Réservé à {@link Avantage#detach()}.
     *
     * @param avantage l'avantage à retirer
     */
    void unlink(Avantage avantage) {
        avantages.remove(avantage);
        LOGGER.info("Removed Avantage from Stock.avantages");
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
