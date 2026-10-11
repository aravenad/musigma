package com.musigma.models;

import com.musigma.models.exception.AvantageException;

import java.io.Serializable;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Avantage représente un avantage associé à un type de ticket
 * et à un stock, avec une quantité spécifique par ticket.
 * <p>
 * Un avantage n'est pris en compte qu'une fois connecté ({@link #connecter()}) :
 * il figure alors à la fois dans les avantages de son type de ticket et dans ceux de son stock.
 */
public final class Avantage implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Logger de la classe Avantage.
     */
    private static final Logger LOGGER = getLogger(Avantage.class);
    /**
     * Type de ticket associé à cet avantage.
     */
    private final TypeTicket typeTicket;
    /**
     * Stock associé à cet avantage.
     */
    private final Stock stock;
    /**
     * Quantité d'objet du stock par ticket.
     */
    private int quantiteParTicket;

    /**
     * Constructeur de la classe Avantage.
     *
     * @param typeTicket       le type de ticket associé à cet avantage
     * @param stock            le stock associé à cet avantage
     * @param quantiteParTicket la quantité de cet avantage par ticket
     * @throws AvantageException si le type de ticket ou le stock est null, si la quantité par ticket
     *                           est inférieure ou égale à zéro ou si elle dépasse un stock limité
     */
    public Avantage(TypeTicket typeTicket, Stock stock, int quantiteParTicket) throws AvantageException {
        LOGGER.fine("Initialized Avantage");
        if (typeTicket == null)
            throw new AvantageException("Le type de ticket est null, doit être défini");
        if (stock == null)
            throw new AvantageException("Le stock est null, doit être défini");
        this.typeTicket = typeTicket;
        this.stock = stock;
        setQuantiteParTicket(quantiteParTicket);
        LOGGER.fine("Created Avantage");
    }

    /**
     * Indique si cet avantage est connecté à son type de ticket et à son stock.
     *
     * @return true si l'avantage est connecté, false sinon
     */
    public boolean estConnecte() {
        return typeTicket.getAvantages().contains(this);
    }

    /**
     * Connecte cet avantage à son type de ticket et son stock.
     *
     * @throws AvantageException si un avantage existe déjà pour ce stock sur ce type de ticket,
     *                           ou si la consommation dépasse un stock limité
     */
    public void connecter() throws AvantageException {
        if (typeTicket.getAvantages().stream().anyMatch(av -> av.getStock() == stock))
            throw new AvantageException("Un avantage identique existe déjà pour ce stock");
        if (stock.depasseLimite(consommation(quantiteParTicket)))
            throw new AvantageException("La quantité par ticket multipliée par le nombre de tickets surpasse le stock limité");
        typeTicket.lier(this);
        stock.lier(this);
        LOGGER.fine("Connected stock and ticket from avantage");
    }

    /**
     * Déconnecte cet avantage de son type de ticket et son stock.
     *
     * @throws AvantageException si l'avantage n'est pas connecté
     */
    public void deconnecter() throws AvantageException {
        if (!estConnecte())
            throw new AvantageException("L'avantage n'est pas connecté");
        detacher();
    }

    /**
     * Retire cet avantage de son type de ticket et de son stock, sans vérification.
     */
    void detacher() {
        typeTicket.delier(this);
        stock.delier(this);
        LOGGER.fine("Disconnected stock and ticket from avantage");
    }

    /**
     * Retourne la quantité d'objet du stock par ticket.
     *
     * @return la quantité d'objet du stock par ticket
     */
    public int getQuantiteParTicket() {
        return quantiteParTicket;
    }

    /**
     * Définit la quantité d'objet du stock par ticket.
     *
     * @param quantiteParTicket la quantité d'objet du stock par ticket
     * @throws AvantageException si la quantité est inférieure ou égale à zéro, ou si elle dépasse un stock limité
     */
    public void setQuantiteParTicket(int quantiteParTicket) throws AvantageException {
        if (quantiteParTicket <= 0)
            throw new AvantageException("La quantité par ticket est inférieure ou égale à zéro, doit être positive");
        long actuelle = estConnecte() ? consommation(this.quantiteParTicket) : 0;
        if (stock.depasseLimite(consommation(quantiteParTicket) - actuelle))
            throw new AvantageException("La quantité par ticket multipliée par le nombre de tickets surpasse le stock limité");
        this.quantiteParTicket = quantiteParTicket;
        LOGGER.fine("Set Avantage.quantiteParTicket");
    }

    /**
     * Retourne la quantité du stock consommée par cet avantage pour tous les tickets de son type.
     *
     * @return le nombre de tickets multiplié par la quantité par ticket
     */
    public long getQuantiteTotale() {
        return consommation(quantiteParTicket);
    }

    /**
     * Retourne la quantité du stock consommée par cet avantage pour une quantité par ticket donnée.
     *
     * @param quantiteParTicket la quantité par ticket
     * @return le nombre de tickets multiplié par la quantité par ticket
     */
    long consommation(int quantiteParTicket) {
        return (long) typeTicket.getQuantite() * quantiteParTicket;
    }

    /**
     * Retourne le type de ticket associé à cet avantage.
     *
     * @return le type de ticket associé à cet avantage
     */
    public TypeTicket getTypeTicket() {
        return typeTicket;
    }

    /**
     * Retourne le stock associé à cet avantage.
     *
     * @return le stock associé à cet avantage
     */
    public Stock getStock() {
        return stock;
    }
}
