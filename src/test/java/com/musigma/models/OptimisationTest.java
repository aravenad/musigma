package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optimisation des ventes de tickets (UC3.2, AUDIT.md M8 à M11).
 */
@DisplayName("Optimisation des ventes")
class OptimisationTest {

    private static Festival festival(float area) throws FestivalException {
        return new Festival("Festival", LocalDateTime.now().plusDays(1), 0, area, "Grenoble");
    }

    /**
     * Exemple du rapport de méthodes numériques (mega-sae/SAE1256_E1-06.pdf) :
     * VIP à 50 € (4 m², 1 gobelet, 1 parking), Non-VIP à 35 € (2 m², 2 gobelets),
     * pour 14 000 m², 8 000 gobelets et 3 000 places de parking.
     */
    @Test
    @DisplayName("M8/M10 — l'exemple du rapport de maths donne 2 000 VIP et 3 000 Non-VIP pour 205 000 €")
    void reportExample() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Festival festival = festival(14000);
        Stock gobelet = new Stock("Gobelet", 8000, true, 1);
        Stock parking = new Stock("Place de parking", 3000, true, 1);
        festival.ajouterStock(gobelet);
        festival.ajouterStock(parking);

        TypeTicket vip = new TypeTicket("VIP", 0, 50);
        vip.setSurface(4);
        TypeTicket nonVip = new TypeTicket("Non-VIP", 0, 35);
        nonVip.setSurface(2);
        festival.ajouterTypeTicket(vip);
        festival.ajouterTypeTicket(nonVip);
        new Avantage(vip, gobelet, 1).connecter();
        new Avantage(vip, parking, 1).connecter();
        new Avantage(nonVip, gobelet, 2).connecter();

        PlanDeVente plan = festival.optimiserVentes();
        assertEquals(205000, plan.recette(), 1e-6);
        assertEquals(2000, plan.quantites().get(vip));
        assertEquals(3000, plan.quantites().get(nonVip));
        assertEquals(0, vip.getQuantite(), "Calculer une recommandation ne modifie pas le festival");

        festival.appliquerPlanDeVente(plan);
        assertEquals(2000, vip.getQuantite());
        assertEquals(3000, nonVip.getQuantite());
    }

    @Test
    @DisplayName("M8 — le nombre de tickets optimal est entier même si l'optimum continu ne l'est pas")
    void fractionalOptimum() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Festival festival = festival(100000);
        Stock gobelet = new Stock("Gobelet", 8001, true, 1);
        festival.ajouterStock(gobelet);
        TypeTicket ticket = new TypeTicket("Simple", 0, 10);
        festival.ajouterTypeTicket(ticket);
        new Avantage(ticket, gobelet, 2).connecter();

        PlanDeVente plan = festival.optimiserVentes();
        assertEquals(4000, plan.quantites().get(ticket), "8001 gobelets à 2 par ticket : 4000 tickets au plus");
        assertEquals(40000, plan.recette(), 1e-6);
    }

    @Test
    @DisplayName("M9 — un ticket sans avantage reste limité par la surface du festival")
    void ticketWithoutAvantageIsBoundedByArea() throws FestivalException, TypeTicketException {
        Festival festival = festival(1000);
        TypeTicket ticket = new TypeTicket("Simple", 0, 10);
        festival.ajouterTypeTicket(ticket);

        PlanDeVente plan = festival.optimiserVentes();
        assertEquals((int) (1000 / TypeTicket.SURFACE_PAR_DEFAUT), plan.quantites().get(ticket));
        assertEquals(10.0 * plan.quantites().get(ticket), plan.recette(), 1e-6);
    }

    @Test
    @DisplayName("Appliquer une recommandation qui réduit un ticket pour en augmenter un autre sur un stock limité")
    void applyPlanSwapsQuantitiesOnLimitedStock() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Festival festival = festival(100000);
        Stock parking = new Stock("Parking", 100, true, 1);
        festival.ajouterStock(parking);
        TypeTicket cheap = new TypeTicket("Pass", 100, 10);
        TypeTicket vip = new TypeTicket("VIP", 0, 50);
        festival.ajouterTypeTicket(cheap);
        festival.ajouterTypeTicket(vip);
        new Avantage(cheap, parking, 1).connecter();
        new Avantage(vip, parking, 1).connecter();

        PlanDeVente plan = festival.optimiserVentes();
        assertEquals(100, plan.quantites().get(vip));
        assertDoesNotThrow(() -> festival.appliquerPlanDeVente(plan), "Passer de 100 Pass à 100 VIP ne doit pas dépasser le parking en cours de route");
        assertEquals(0, cheap.getQuantite());
        assertEquals(100, vip.getQuantite());
    }

    @Test
    @DisplayName("Optimiser sans type de ticket est signalé")
    void noTicketType() throws FestivalException {
        assertThrows(FestivalException.class, () -> festival(1000).optimiserVentes());
    }
}
