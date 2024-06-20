package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvantageTest {

    TypeTicket ticket;
    Stock stock;
    Avantage avantage;

    @BeforeEach
    void setUp() throws StockException, TypeTicketException, AvantageException {
        ticket = new TypeTicket("Ticket Type", 40, 45);
        stock = new Stock("Stock Name", 1000, true, 1000);
        avantage = new Avantage(ticket, stock, 2);
    }

    @Test
    void setQuantityByTicket() {
        assertDoesNotThrow(() -> avantage.setQuantityByTicket(4), "Le prix de location de l'emplacement du festival doit pouvoir être changé par un nouveau valide");
        assertEquals(4, avantage.getQuantityByTicket(), "Le prix de location de l'emplacement du festival doit être changé par le nouveau valide");
        assertThrows(AvantageException.class, () -> avantage.setQuantityByTicket(-100), "Le prix de location de l'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car négatif");
        stock.setFixed(true);
        assertThrows(AvantageException.class, () -> avantage.setQuantityByTicket(100), "La quantité par ticket multipié par le nombre de ticket ne doit pas surpasser les stocks limités");
        stock.setFixed(false);
        assertDoesNotThrow(() -> avantage.setQuantityByTicket(100), "La quantité par ticket multipié par le nombre de ticket peut surpasser les stocks illimités");
    }

    @Test
    void add() {
        assertDoesNotThrow(() -> avantage.connect(), "Un avantage doit pouvoir lié son stock et son type de ticket");
        assertTrue(stock.getAvantages().contains(avantage), "L'avantage doit s'être ajouté aux avantages du stock");
        assertTrue(ticket.getAvantages().contains(avantage), "L'avantage doit s'être ajouté aux avantages du type de ticket");
        assertThrows(AvantageException.class, () -> avantage.connect(), "Un avantage ne doit pas pouvoir lié plus d'une fois son stock et son type de ticket");
    }

    @Test
    void remove() throws TypeTicketException, AvantageException, StockException {
        assertThrows(TypeTicketException.class, () -> avantage.disconnect(), "Un avantage ne doit pouvoir délié son stock et son type de ticket si ils ne le sont pas préalablement");
        avantage.connect();
        assertDoesNotThrow(() -> avantage.disconnect(), "Un avantage doit pouvoir délié son stock et son type de ticket");
        assertFalse(stock.getAvantages().contains(avantage), "L'avantage doit s'être retiré des avantages du stock");
        assertFalse(ticket.getAvantages().contains(avantage), "L'avantage doit s'être retiré des avantages du type de ticket");
    }
}