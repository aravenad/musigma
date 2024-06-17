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
        stock = new Stock("Stock Name", 1000, true);
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
}