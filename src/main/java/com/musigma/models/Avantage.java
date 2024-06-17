package com.musigma.models;

import com.musigma.models.exception.AvantageException;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;

import static fr.uga.iut2.musigma.util.Log.getLogger;

/**
 * La classe Avantage représente un avantage associé à un type de ticket 
 * et à un stock, avec une quantité spécifique par ticket.
 */
public class Avantage implements Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(Avantage.class);

    // Quantité de cet avantage par ticket
    private int quantityByTicket;

    // Type de ticket associé à cet avantage
    private final TypeTicket ticketType;

    // Stock associé à cet avantage
    private final Stock stock;

    /**
     * Constructeur de la classe Avantage.
     *
     * @param ticketType le type de ticket associé à cet avantage
     * @param stock le stock associé à cet avantage
     * @param quantityByTicket la quantité de cet avantage par ticket
     */
    public Avantage(TypeTicket ticketType, Stock stock, int quantityByTicket) throws AvantageException {
        LOGGER.info(String.format("Initialized Avantage 0x%x", super.hashCode()));
        this.ticketType = ticketType;
        this.stock = stock;
        setQuantityByTicket(quantityByTicket);
        LOGGER.info(String.format("Created Avantage 0x%x", super.hashCode()));
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
     * @throws AvantageException si la quantité est inférieure ou égale à zéro
     */
    public void setQuantityByTicket(int quantityByTicket) throws AvantageException {
        if (quantityByTicket <= 0)
            throw new AvantageException("La quantité par ticket est inférieure ou égale à zéro, doit être positive");
        if (stock.isFixed() && quantityByTicket * ticketType.getQuantity() > stock.getQuantity())
            throw new AvantageException("La quantité par ticket multipié par le nombre de ticket surpasse les stocks");
        this.quantityByTicket = quantityByTicket;
        LOGGER.info(String.format("Set Avantage.quantityByTicket 0x%x", super.hashCode()));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avantage avantage = (Avantage) o;
        return quantityByTicket == avantage.quantityByTicket && Objects.equals(ticketType, avantage.ticketType) && Objects.equals(stock, avantage.stock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantityByTicket, ticketType, stock);
    }
}
