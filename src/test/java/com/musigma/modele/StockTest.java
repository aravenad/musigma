package com.musigma.modele;

import static org.junit.jupiter.api.Assertions.*;

import com.musigma.modele.Avantage;
import com.musigma.modele.Stock;
import com.musigma.modele.TypeTicket;
import com.musigma.modele.exception.AvantageException;
import com.musigma.modele.exception.StockException;
import com.musigma.modele.exception.TypeTicketException;
import org.junit.jupiter.api.*;

class StockTest {

    Stock stock;
    Avantage avantage;
    TypeTicket ticket;

    @BeforeEach
    void setUp() throws StockException, TypeTicketException, AvantageException {
        stock = new Stock("Stock Name", 100, true);
        ticket = new TypeTicket("BIP", 100, 60);
        avantage = new Avantage(ticket, stock, 1);
    }

    @Test
    void setName() {
        assertDoesNotThrow(() -> stock.setName("New Stock Name"), "Le stock doit pouvoir être renommé par un nom valide");
        assertEquals("New Stock Name", stock.getName(), "Le stock doit être renommé par le nom valide");
        assertThrows(StockException.class, () -> stock.setName(null), "Le stock ne doit pas pouvoir être renommé par un nom invalide car null");
        assertThrows(StockException.class, () -> stock.setName(""), "Le stock ne doit pas pouvoir être renommé par un nom invalide car vide");
    }

    @Test
    void setAbsoluteQuantity() {
        stock.setFixed(true);
        assertThrows(StockException.class, () -> stock.setQuantity(200), "Le stock ne doit pas pouvoir être requantifié tant qu'il est fixé");
        stock.setFixed(false);
        assertDoesNotThrow(() -> stock.setQuantity(200), "Le stock doit pouvoir être requantifié par une quantité valide");
        assertEquals(200,  stock.getQuantity(), "Le stock doit être requantifié par la quantité valide");
        assertThrows(StockException.class, () -> stock.setQuantity(0), "Le stock ne doit pas pouvoir être requantifié par une quantité invalide car nulle (0)");
        assertThrows(StockException.class, () -> stock.setQuantity(-50), "Le stock ne doit pas pouvoir être requantifié par une quantité invalide car négative");
    }

    @Test
    void addAvantage() {
        assertDoesNotThrow(() -> stock.addAvantage(avantage), "Le stock doit pouvoir ajouté un avantage");
        assertTrue(stock.getAvantages().contains(avantage), "Le stock doit avoir ajouté l'avantage");
        assertThrows(StockException.class, () -> stock.addAvantage(null), "Le stock ne doit pas pouvoir ajouter null comme avantage");
        assertFalse(stock.getAvantages().contains(null), "Le stock ne doit pas avoir ajouté null comme un nouvel avantage");
    }

    @Test
    void removeAvantage() throws StockException {
        stock.addAvantage(avantage);
        assertDoesNotThrow(() -> stock.removeAvantage(avantage), "Le stock doit pouvoir retirer un de ses avantages");
        assertFalse(stock.getAvantages().contains(avantage), "Le stock doit avoir retirer l'avantage de siens");
        assertThrows(StockException.class, () -> stock.removeAvantage(avantage), "Le stock ne doit pas pouvoir retirer un avantage n'étant pas des siens");
    }

    @Test
    void setFixed() {
        stock.setFixed(true);
        assertTrue(stock.isFixed(), "Le stock doit avoir été fixé");
        stock.setFixed(false);
        assertFalse(stock.isFixed(), "Le stock doit avoir été défixé");
    }
}