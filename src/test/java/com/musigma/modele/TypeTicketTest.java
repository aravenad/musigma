package com.musigma.modele;

import com.musigma.modele.Avantage;
import com.musigma.modele.Stock;
import com.musigma.modele.TypeTicket;
import com.musigma.modele.exception.AvantageException;
import com.musigma.modele.exception.StockException;
import com.musigma.modele.exception.TypeTicketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeTicketTest {

    TypeTicket ticket;
    Stock stock;
    Avantage avantage;

    @BeforeEach
    void setUp() throws StockException, TypeTicketException, AvantageException {
        stock = new Stock("Stock Name", 100, true);
        ticket = new TypeTicket("Ticket Type Name", 40, 45);
        avantage = new Avantage(ticket, stock, 2);
    }

    @Test
    void setType() {
        assertDoesNotThrow(() -> ticket.setType("New Ticket Type Name"), "Le type de ticket doit pouvoir être retypé par un type valide");
        assertEquals("New Ticket Type Name", ticket.getType(), "Le type de ticket doit être retypé par le type valide");
        assertThrows(TypeTicketException.class, () -> ticket.setType(null), "Le type de ticket ne doit pas pouvoir être retypé par un type invalide car null");
        assertThrows(TypeTicketException.class, () -> ticket.setType(""), "Le type de ticket ne doit pas pouvoir être retypé par un type invalide car vide");
    }

    @Test
    void setQuantity() throws TypeTicketException {
        assertDoesNotThrow(() -> ticket.setQuantity(10), "Le nombre de ticket doit pouvoir être changé par un nouveau valide");
        assertEquals(10, ticket.getQuantity(), "Le nombre de ticket doit être changé par le nouveau valide");
        assertThrows(TypeTicketException.class, () -> ticket.setQuantity(-100), "Le nombre de ticket ne doit pas pouvoir être changé par un nouveau invalide car négatif");
        stock.setFixed(true);
        ticket.addAvantage(avantage);
        assertThrows(TypeTicketException.class, () -> ticket.setQuantity(10000), "La quantité par ticket multipié par le nombre de ticket ne doit pas surpasser les stocks limités");
        stock.setFixed(false);
        assertDoesNotThrow(() -> avantage.setQuantityByTicket(10000), "La quantité par ticket multipié par le nombre de ticket peut surpasser les stocks illimités");
    }

    @Test
    void setPrice() {
        assertDoesNotThrow(() -> ticket.setPrice(200), "Le prix du ticket doit pouvoir être changé par un nouveau valide");
        assertEquals(200, ticket.getPrice(), "Le prix du ticket doit être changé par le nouveau valide");
        assertThrows(TypeTicketException.class, () -> ticket.setPrice(-100), "Le prix du ticket ne doit pas pouvoir être changé par un nouveau invalide car négatif");
    }

    @Test
    void addAvantage() {
        assertDoesNotThrow(() -> ticket.addAvantage(avantage), "Le type de ticket doit pouvoir ajouté un avantage");
        assertTrue(ticket.getAvantages().contains(avantage), "Le type de ticket doit avoir ajouté l'avantage");
        assertThrows(TypeTicketException.class, () -> ticket.addAvantage(null), "Le type de ticket ne doit pas pouvoir ajouter null comme avantage");
        assertFalse(ticket.getAvantages().contains(null), "Le type de ticket ne doit pas avoir ajouté null comme un nouvel avantage");
    }

    @Test
    void removeAvantage() throws TypeTicketException {
        ticket.addAvantage(avantage);
        assertDoesNotThrow(() -> ticket.removeAvantage(avantage), "Le type de ticket doit pouvoir retirer un de ses avantages");
        assertFalse(ticket.getAvantages().contains(avantage), "Le type de ticket doit avoir retirer l'avantage de siens");
        assertThrows(TypeTicketException.class, () -> ticket.removeAvantage(avantage), "Le type de ticket ne doit pas pouvoir retirer un avantage n'étant pas des siens");
    }
}