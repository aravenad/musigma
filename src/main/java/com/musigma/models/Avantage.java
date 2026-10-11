package com.musigma.models;

import com.musigma.models.exception.AvantageException;

import java.io.Serializable;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Avantage représente un avantage associé à un type de ticket
 * et à un stock, avec une quantité spécifique par ticket.
 * <p>
 * Un avantage n'est pris en compte qu'une fois connecté ({@link #connect()}) :
 * il figure alors à la fois dans les avantages de son type de ticket et dans ceux de son stock.
 */
public final class Avantage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger de la classe Avantage.
     */
    private static final Logger LOGGER = getLogger(Avantage.class);
    /**
     * Type de ticket associé à cet avantage.
     */
    private final TypeTicket ticketType;
    /**
     * Stock associé à cet avantage.
     */
    private final Stock stock;
    /**
     * Quantité d'objet du stock par ticket.
     */
    private int quantityByTicket;

    /**
     * Constructeur de la classe Avantage.
     *
     * @param ticketType       le type de ticket associé à cet avantage
     * @param stock            le stock associé à cet avantage
     * @param quantityByTicket la quantité de cet avantage par ticket
     * @throws AvantageException si le type de ticket ou le stock est null, si la quantité par ticket
     *                           est inférieure ou égale à zéro ou si elle dépasse un stock limité
     */
    public Avantage(TypeTicket ticketType, Stock stock, int quantityByTicket) throws AvantageException {
        LOGGER.fine("Initialized Avantage");
        if (ticketType == null)
            throw new AvantageException("Le type de ticket est null, doit être défini");
        if (stock == null)
            throw new AvantageException("Le stock est null, doit être défini");
        this.ticketType = ticketType;
        this.stock = stock;
        setQuantityByTicket(quantityByTicket);
        LOGGER.fine("Created Avantage");
    }

    /**
     * Indique si cet avantage est connecté à son type de ticket et à son stock.
     *
     * @return true si l'avantage est connecté, false sinon
     */
    public boolean isConnected() {
        return ticketType.getAvantages().contains(this);
    }

    /**
     * Connecte cet avantage à son type de ticket et son stock.
     *
     * @throws AvantageException si un avantage existe déjà pour ce stock sur ce type de ticket,
     *                           ou si la consommation dépasse un stock limité
     */
    public void connect() throws AvantageException {
        if (ticketType.getAvantages().stream().anyMatch(av -> av.getStock() == stock))
            throw new AvantageException("Un avantage identique existe déjà pour ce stock");
        if (stock.exceedsLimit(contribution(quantityByTicket)))
            throw new AvantageException("La quantité par ticket multipliée par le nombre de tickets surpasse le stock limité");
        ticketType.link(this);
        stock.link(this);
        LOGGER.fine("Connected stock and ticket from avantage");
    }

    /**
     * Déconnecte cet avantage de son type de ticket et son stock.
     *
     * @throws AvantageException si l'avantage n'est pas connecté
     */
    public void disconnect() throws AvantageException {
        if (!isConnected())
            throw new AvantageException("L'avantage n'est pas connecté");
        detach();
    }

    /**
     * Retire cet avantage de son type de ticket et de son stock, sans vérification.
     */
    void detach() {
        ticketType.unlink(this);
        stock.unlink(this);
        LOGGER.fine("Disconnected stock and ticket from avantage");
    }

    /**
     * Retourne la quantité d'objet du stock par ticket.
     *
     * @return la quantité d'objet du stock par ticket
     */
    public int getQuantityByTicket() {
        return quantityByTicket;
    }

    /**
     * Définit la quantité d'objet du stock par ticket.
     *
     * @param quantityByTicket la quantité d'objet du stock par ticket
     * @throws AvantageException si la quantité est inférieure ou égale à zéro, ou si elle dépasse un stock limité
     */
    public void setQuantityByTicket(int quantityByTicket) throws AvantageException {
        if (quantityByTicket <= 0)
            throw new AvantageException("La quantité par ticket est inférieure ou égale à zéro, doit être positive");
        long current = isConnected() ? contribution(this.quantityByTicket) : 0;
        if (stock.exceedsLimit(contribution(quantityByTicket) - current))
            throw new AvantageException("La quantité par ticket multipliée par le nombre de tickets surpasse le stock limité");
        this.quantityByTicket = quantityByTicket;
        LOGGER.fine("Set Avantage.quantityByTicket");
    }

    /**
     * Retourne la quantité du stock consommée par cet avantage pour tous les tickets de son type.
     *
     * @return le nombre de tickets multiplié par la quantité par ticket
     */
    public long getTotalQuantity() {
        return contribution(quantityByTicket);
    }

    /**
     * Retourne la quantité du stock consommée par cet avantage pour une quantité par ticket donnée.
     *
     * @param quantityByTicket la quantité par ticket
     * @return le nombre de tickets multiplié par la quantité par ticket
     */
    long contribution(int quantityByTicket) {
        return (long) ticketType.getQuantity() * quantityByTicket;
    }

    /**
     * Retourne le type de ticket associé à cet avantage.
     *
     * @return le type de ticket associé à cet avantage
     */
    public TypeTicket getTicketType() {
        return ticketType;
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
