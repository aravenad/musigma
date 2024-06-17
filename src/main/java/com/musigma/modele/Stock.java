package com.musigma.modele;

import com.musigma.modele.exception.StockException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static fr.uga.iut2.musigma.util.Log.getLogger;
import static java.util.logging.Level.INFO;

/**
 * La classe Stock représente un stock avec un nom, une quantité absolue,
 * et une liste d'avantages.
 */
public class Stock implements Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(Serializable.class);

    // Nom du stock
    private String name;

    // Quantité du stock
    private int quantity;

    // Si la quantité du stock est fixe
    private boolean fixed;

    // Liste des avantages associés à ce stock
    private final ArrayList<Avantage> avantages;

    /**
     * Constructeur de la classe Stock.
     *
     * @param name Nom du stock
     * @param quantity Quantité absolue du stock
     * @param quantity Si la quantité du stock est fixe
     */
    public Stock(String name, int quantity, boolean fixed) throws StockException {
        LOGGER.info(String.format("Initialized Stock 0x%x", super.hashCode()));
        avantages = new ArrayList<>();
        setName(name);
        setQuantity(quantity);
        setFixed(fixed);
        LOGGER.info(String.format("Created Stock 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Stock.name 0x%x", super.hashCode()));
    }

    /**
     * Retourne si la quantité du stock est fixe ou non.
     *
     * @return true si la quantité est fixe, false sinon
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Définit si la quantité du stock est fixe ou non.
     *
     * @param fixed true pour fixer la quantité, false pour la rendre non fixe
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
        LOGGER.info(String.format("Set Stock.fixed 0x%x", super.hashCode()));
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
     * @throws StockException si la quantité est négative ou nulle (0)
     */
    public void setQuantity(int quantity) throws StockException {
        if (fixed) {
            throw new StockException("La quantité est fixée, doit préalablement être défixée");
        }
        if (quantity <= 0) {
            throw new StockException("La quantité absolue est négative ou nulle (0), doit être défini");
        }
        this.quantity = quantity;
        LOGGER.info(String.format("Set Stock.quantity 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des avantages associés à ce stock.
     *
     * @return la liste des avantages
     */
    public ArrayList<Avantage> getAvantages() {
        return avantages;
    }

    /**
     * Ajoute un avantage à la liste des avantages de ce stock.
     *
     * @param avantage l'avantage à ajouter
     * @throws StockException si l'avantage est null
     */
    public void addAvantage(Avantage avantage) throws StockException {
        if (avantage == null)
            throw new StockException("L'avantage est null, doit être défini");
        avantages.add(avantage);
        LOGGER.info(String.format("Added Avantage to Stock.avantages 0x%x", super.hashCode()));
    }

    /**
     * Supprime un avantage de la liste des avantages de ce stock.
     *
     * @param avantage l'avantage à supprimer
     * @throws StockException si l'avantage n'est pas trouvé dans la liste
     */
    public void removeAvantage(Avantage avantage) throws StockException {
        if (!avantages.remove(avantage))
            throw new StockException("L'avantage n'a pas été trouvé");
        LOGGER.info(String.format("Removed Avantage from Stock.avantages 0x%x", super.hashCode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return quantity == stock.quantity && fixed == stock.fixed && Objects.equals(name, stock.name) && Objects.equals(avantages, stock.avantages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, fixed, avantages);
    }
}
