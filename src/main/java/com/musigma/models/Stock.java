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
public final class Stock implements Serializable {

    private static final long serialVersionUID = 2L;

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
    private String nom;
    /**
     * Quantité du stock.
     */
    private int quantite;
    /**
     * Si la quantité du stock est une limite que les tickets ne peuvent pas dépasser.
     */
    private boolean limite;
    /**
     * Prix du stock.
     */
    private double prix;

    /**
     * Constructeur de la classe Stock.
     *
     * @param nom     Nom du stock
     * @param quantite Quantité absolue du stock
     * @param limite    Si la quantité du stock est une limite pour les tickets
     * @param prix     Prix du stock
     * @throws StockException si le nom est null ou vide, si la quantité est négative ou nulle (0)
     */
    public Stock(String nom, int quantite, boolean limite, double prix) throws StockException {
        LOGGER.fine("Initialized Stock");
        avantages = new ArrayList<>();
        setNom(nom);
        setQuantite(quantite);
        setLimite(limite);
        setPrix(prix);
        LOGGER.fine("Created Stock");
    }

    /**
     * Retourne le nom du stock.
     *
     * @return le nom du stock
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du stock.
     *
     * @param nom le nom du stock
     * @throws StockException si le nom est null ou vide
     */
    public void setNom(String nom) throws StockException {
        if (nom == null || nom.isBlank())
            throw new StockException("Le nom est null ou vide, doit être défini");
        this.nom = nom;
        LOGGER.fine("Set Stock.nom");
    }

    /**
     * Retourne si la quantité du stock est une limite pour les tickets.
     *
     * @return true si le stock est limité, false sinon
     */
    public boolean estLimite() {
        return limite;
    }

    /**
     * Définit si la quantité du stock est une limite pour les tickets.
     *
     * @param limite true pour limiter les tickets à la quantité du stock, false sinon
     * @throws StockException si les tickets consomment déjà plus que la quantité du stock
     */
    public void setLimite(boolean limite) throws StockException {
        if (limite && getQuantiteConsommee() > quantite)
            throw new StockException(String.format(
                    "Les tickets consomment déjà %d unités, le stock de %d ne peut pas être limité",
                    getQuantiteConsommee(), quantite
            ));
        this.limite = limite;
        LOGGER.fine("Set Stock.limite");
    }

    /**
     * Retourne la quantité absolue du stock.
     *
     * @return la quantité absolue du stock
     */
    public int getQuantite() {
        return quantite;
    }

    /**
     * Définit la quantité absolue du stock.
     *
     * @param quantite la quantité absolue du stock
     * @throws StockException si la quantité est négative ou nulle (0),
     *                        ou inférieure à ce que consomment les tickets d'un stock limité
     */
    public void setQuantite(int quantite) throws StockException {
        if (quantite <= 0) {
            throw new StockException("La quantité absolue est négative ou nulle (0), doit être défini");
        }
        if (limite && getQuantiteConsommee() > quantite) {
            throw new StockException(String.format(
                    "Les tickets consomment %d unités, le stock limité ne peut pas descendre à %d",
                    getQuantiteConsommee(), quantite
            ));
        }
        this.quantite = quantite;
        LOGGER.fine("Set Stock.quantite");
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
     * @throws StockException si le prix est négatif
     */
    public void setPrix(double prix) throws StockException {
        if (prix < 0) {
            throw new StockException("Le prix est négatif, doit être positif ou nul");
        }
        this.prix = prix;
        LOGGER.fine("Set Stock.prix");
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
    public long getQuantiteConsommee() {
        return avantages.stream()
                .mapToLong(avantage -> avantage.consommation(avantage.getQuantiteParTicket()))
                .sum();
    }

    /**
     * Retourne la quantité nécessaire de ce stock (UC3.3) : la quantité d'un stock limité,
     * sinon la plus grande valeur entre la quantité prévue et la consommation des tickets.
     *
     * @return la quantité nécessaire
     */
    public long getQuantiteNecessaire() {
        return limite ? quantite : Math.max(quantite, getQuantiteConsommee());
    }

    /**
     * Retourne la quantité manquante pour couvrir la consommation des tickets.
     *
     * @return la consommation moins la quantité prévue, ou 0 si la quantité prévue suffit
     */
    public long getQuantiteManquante() {
        return Math.max(0, getQuantiteConsommee() - quantite);
    }

    /**
     * Indique si une consommation supplémentaire dépasserait la quantité d'un stock limité.
     *
     * @param consommationSupplementaire la consommation ajoutée (négative si elle diminue)
     * @return true si le stock est limité et serait dépassé, false sinon
     */
    boolean depasseLimite(long consommationSupplementaire) {
        return limite && consommationSupplementaire > 0 && getQuantiteConsommee() + consommationSupplementaire > quantite;
    }

    /**
     * Ajoute un avantage à ce stock en le connectant aussi à son type de ticket.
     * Sans effet si l'avantage est déjà connecté.
     *
     * @param avantage l'avantage à ajouter
     * @throws StockException    si l'avantage est null ou concerne un autre stock
     * @throws AvantageException si l'avantage ne peut pas être connecté
     * @see Avantage#connecter()
     */
    public void ajouterAvantage(Avantage avantage) throws StockException, AvantageException {
        if (avantage == null)
            throw new StockException("L'avantage est null, doit être défini");
        if (avantage.getStock() != this)
            throw new StockException("L'avantage concerne un autre stock");
        if (avantages.contains(avantage))
            return;
        avantage.connecter();
    }

    /**
     * Supprime un avantage de ce stock et de son type de ticket.
     *
     * @param avantage l'avantage à supprimer
     * @throws StockException si l'avantage n'est pas trouvé dans la liste
     * @see Avantage#deconnecter()
     */
    public void retirerAvantage(Avantage avantage) throws StockException {
        if (!avantages.contains(avantage))
            throw new StockException("L'avantage n'a pas été trouvé");
        avantage.detacher();
    }

    /**
     * Ajoute l'avantage à la liste, sans vérification. Réservé à {@link Avantage#connecter()}.
     *
     * @param avantage l'avantage à ajouter
     */
    void lier(Avantage avantage) {
        avantages.add(avantage);
        LOGGER.fine("Added Avantage to Stock.avantages");
    }

    /**
     * Retire l'avantage de la liste, sans vérification. Réservé à {@link Avantage#detacher()}.
     *
     * @param avantage l'avantage à retirer
     */
    void delier(Avantage avantage) {
        avantages.remove(avantage);
        LOGGER.fine("Removed Avantage from Stock.avantages");
    }

    @Override
    public String toString() {
        return this.getNom();
    }

}
