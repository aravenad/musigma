package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Défauts connus de l'optimisation des ventes (AUDIT.md §3.3), UC3.2.
 * Ces tests échouent tant que les défauts ne sont pas corrigés : lancer avec {@code mvn test -Pbugs}.
 */
@Tag("bug")
@DisplayName("Défauts connus — optimisation des ventes")
class OptimisationBugTest {

    /**
     * Exemple du rapport de méthodes numériques (mega-sae/SAE1256_E1-06.pdf) :
     * VIP à 50 € (4 m², 1 gobelet, 1 parking), Non-VIP à 35 € (2 m², 2 gobelets),
     * pour 14 000 m², 8 000 gobelets et 3 000 places de parking.
     * La surface par ticket est modélisée par un stock limité, faute de mieux dans le modèle actuel (M10).
     */
    @Test
    @DisplayName("M8 — l'exemple du rapport de maths donne 2 000 VIP et 3 000 Non-VIP pour 205 000 €")
    void reportExample() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Festival festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 14000, "Grenoble");
        Stock surface = new Stock("Surface", 14000, true, 1);
        Stock gobelet = new Stock("Gobelet", 8000, true, 1);
        Stock parking = new Stock("Place de parking", 3000, true, 1);
        festival.addStock(surface);
        festival.addStock(gobelet);
        festival.addStock(parking);

        TypeTicket vip = new TypeTicket("VIP", 0, 50);
        TypeTicket nonVip = new TypeTicket("Non-VIP", 0, 35);
        festival.addTicketType(vip);
        festival.addTicketType(nonVip);
        new Avantage(vip, surface, 4).connect();
        new Avantage(vip, gobelet, 1).connect();
        new Avantage(vip, parking, 1).connect();
        new Avantage(nonVip, surface, 2).connect();
        new Avantage(nonVip, gobelet, 2).connect();

        double result = assertDoesNotThrow(festival::optimizeResult, "Le bruit numérique du solveur ne doit pas faire échouer l'arrondi");
        assertEquals(205000, result, 1e-6);
        assertEquals(2000, vip.getQuantity());
        assertEquals(3000, nonVip.getQuantity());
    }

    @Test
    @DisplayName("M8 — le nombre de tickets optimal est entier même si l'optimum continu ne l'est pas")
    void fractionalOptimum() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Festival festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 100000, "Grenoble");
        Stock gobelet = new Stock("Gobelet", 8001, true, 1);
        festival.addStock(gobelet);
        TypeTicket ticket = new TypeTicket("Simple", 0, 10);
        festival.addTicketType(ticket);
        new Avantage(ticket, gobelet, 2).connect();

        double result = assertDoesNotThrow(festival::optimizeResult);
        assertEquals(4000, ticket.getQuantity(), "8001 gobelets à 2 par ticket : 4000 tickets au plus");
        assertEquals(40000, result, 1e-6);
    }

    @Test
    @DisplayName("M9 — un ticket sans avantage reste limité par la surface du festival")
    void ticketWithoutAvantageIsBoundedByArea() throws FestivalException, TypeTicketException {
        Festival festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 1000, "Grenoble");
        TypeTicket ticket = new TypeTicket("Simple", 0, 10);
        festival.addTicketType(ticket);

        double result = festival.optimizeResult();
        int maxPeople = (int) (1000 / 0.42);
        assertTrue(ticket.getQuantity() > 0 && ticket.getQuantity() <= maxPeople,
                "Quantité attendue entre 1 et " + maxPeople + ", obtenue " + ticket.getQuantity());
        assertEquals(10.0 * ticket.getQuantity(), result, 1e-6, "La recette doit correspondre aux tickets vendus");
    }
}
