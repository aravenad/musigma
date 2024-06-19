package com.musigma.models;

import com.musigma.models.exception.StockException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

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

    // Prix du stock
    private double prix;

    // Liste des avantages associés à ce stock
    private final ArrayList<Avantage> avantages;

    /**
     * Constructeur de la classe Stock.
     *
     * @param name Nom du stock
     * @param quantity Quantité absolue du stock
     * @param quantity Si la quantité du stock est fixe
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
        LOGGER.info("Added Avantage to Stock.avantages");
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
        LOGGER.info("Removed Avantage from Stock.avantages");
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

    @Override
    public String toString(){
        return this.getName();
    }

}
