package com.musigma.models;

import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.StockException;
import com.musigma.models.exception.TypeTicketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    Stock stock;
    Avantage avantage;
    TypeTicket ticket;

    @BeforeEach
    void setUp() throws StockException, TypeTicketException, AvantageException {
        stock = new Stock("Stock Name", 100, true, 1000);
        ticket = new TypeTicket("BIP", 100, 60);
        avantage = new Avantage(ticket, stock, 1);
    }

    @Test
    void setNom() {
        assertDoesNotThrow(() -> stock.setNom("New Stock Name"), "Le stock doit pouvoir être renommé par un nom valide");
        assertEquals("New Stock Name", stock.getNom(), "Le stock doit être renommé par le nom valide");
        assertThrows(StockException.class, () -> stock.setNom(null), "Le stock ne doit pas pouvoir être renommé par un nom invalide car null");
        assertThrows(StockException.class, () -> stock.setNom(""), "Le stock ne doit pas pouvoir être renommé par un nom invalide car vide");
    }

    @Test
    void setQuantite() throws AvantageException {
        avantage.connecter(); // 100 tickets consomment 100 unités du stock limité
        assertDoesNotThrow(() -> stock.setQuantite(150), "Un stock limité doit pouvoir être requantifié au-dessus de sa consommation");
        assertThrows(StockException.class, () -> stock.setQuantite(50), "Un stock limité ne doit pas descendre sous ce que consomment les tickets");
        assertDoesNotThrow(() -> stock.setLimite(false));
        assertDoesNotThrow(() -> stock.setQuantite(200), "Le stock doit pouvoir être requantifié par une quantité valide");
        assertEquals(200, stock.getQuantite(), "Le stock doit être requantifié par la quantité valide");
        assertThrows(StockException.class, () -> stock.setQuantite(0), "Le stock ne doit pas pouvoir être requantifié par une quantité invalide car nulle (0)");
        assertThrows(StockException.class, () -> stock.setQuantite(-50), "Le stock ne doit pas pouvoir être requantifié par une quantité invalide car négative");
    }

    @Test
    void ajouterAvantage() {
        assertDoesNotThrow(() -> stock.ajouterAvantage(avantage), "Le stock doit pouvoir ajouté un avantage");
        assertTrue(stock.getAvantages().contains(avantage), "Le stock doit avoir ajouté l'avantage");
        assertThrows(StockException.class, () -> stock.ajouterAvantage(null), "Le stock ne doit pas pouvoir ajouter null comme avantage");
        assertFalse(stock.getAvantages().contains(null), "Le stock ne doit pas avoir ajouté null comme un nouvel avantage");
    }

    @Test
    void retirerAvantage() throws StockException, AvantageException {
        stock.ajouterAvantage(avantage);
        assertDoesNotThrow(() -> stock.retirerAvantage(avantage), "Le stock doit pouvoir retirer un de ses avantages");
        assertFalse(stock.getAvantages().contains(avantage), "Le stock doit avoir retirer l'avantage de siens");
        assertFalse(ticket.getAvantages().contains(avantage), "Le type de ticket doit aussi avoir perdu l'avantage");
        assertThrows(StockException.class, () -> stock.retirerAvantage(avantage), "Le stock ne doit pas pouvoir retirer un avantage n'étant pas des siens");
    }

    @Test
    void setLimite() throws StockException, AvantageException, TypeTicketException {
        stock.setLimite(true);
        assertTrue(stock.estLimite(), "Le stock doit avoir été fixé");
        stock.setLimite(false);
        assertFalse(stock.estLimite(), "Le stock doit avoir été défixé");

        avantage.connecter();
        ticket.setQuantite(150); // possible car le stock n'est plus limité
        assertThrows(StockException.class, () -> stock.setLimite(true), "Un stock ne doit pas pouvoir être limité sous ce que consomment déjà les tickets");
    }
}