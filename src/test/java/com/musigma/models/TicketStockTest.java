package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Régressions des tickets et des stocks (AUDIT.md §3.3 et C9), UC3.1 et UC3.3.
 */
@DisplayName("Tickets et stocks")
class TicketStockTest {

    Festival festival;
    Stock parking;
    TypeTicket vip;

    @BeforeEach
    void setUp() throws FestivalException, StockException, TypeTicketException {
        festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 1000, "Grenoble");
        parking = new Stock("Place de parking", 100, false, 5);
        festival.ajouterStock(parking);
        vip = new TypeTicket("VIP", 0, 50);
        festival.ajouterTypeTicket(vip);
    }

    @Test
    @DisplayName("M12 — un stock limité partagé par plusieurs tickets n'est pas dépassé au total")
    void sharedLimitedStockIsNotExceeded() throws Exception {
        TypeTicket pass = new TypeTicket("Pass", 0, 30);
        festival.ajouterTypeTicket(pass);
        new Avantage(vip, parking, 1).connecter();
        new Avantage(pass, parking, 1).connecter();
        parking.setLimite(true);

        vip.setQuantite(80);
        assertThrows(TypeTicketException.class, () -> pass.setQuantite(80), "80 + 80 places de parking dépassent le stock de 100");
    }

    @Test
    @DisplayName("M13 — la quantité d'un stock limité reste modifiable")
    void limitedStockQuantityCanChange() throws StockException {
        parking.setLimite(true);
        assertDoesNotThrow(() -> parking.setQuantite(200));
        assertEquals(200, parking.getQuantite());
    }

    @Test
    @DisplayName("C9 — retirer un avantage d'un ticket le retire aussi de son stock")
    void removeAvantageKeepsStockConsistent() throws Exception {
        Avantage avantage = new Avantage(vip, parking, 1);
        avantage.connecter();
        vip.retirerAvantage(avantage);
        assertFalse(parking.getAvantages().contains(avantage), "Le stock ne doit pas garder un avantage orphelin");
    }

    @Test
    @DisplayName("C9 — retirer un type de ticket détache ses avantages des stocks")
    void removeTicketTypeDisconnectsAvantages() throws Exception {
        Avantage avantage = new Avantage(vip, parking, 1);
        avantage.connecter();
        festival.supprimerTypeTicket(vip);
        assertFalse(parking.getAvantages().contains(avantage), "Le stock ne doit pas garder l'avantage d'un ticket supprimé");
    }

    @Test
    @DisplayName("C9 — la suppression d'un stock est atomique : en cas d'échec, le stock reste dans le festival")
    void removeStockIsAtomic() throws Exception {
        Avantage avantage = new Avantage(vip, parking, 1);
        avantage.connecter();
        vip.retirerAvantage(avantage); // ce que fait le bouton « Supprimer » de l'écran Tickets

        boolean removed;
        try {
            festival.supprimerStock(parking);
            removed = true;
        } catch (FestivalException e) {
            removed = false;
        }
        assertEquals(!removed, festival.getStocks().contains(parking), "Le stock ne doit être retiré que si la suppression réussit");
    }

    @Test
    @DisplayName("C10 — modifier un type de ticket est atomique et garde les noms uniques")
    void updateTicketTypeIsAtomic() throws Exception {
        TypeTicket pass = new TypeTicket("Pass", 0, 30);
        festival.ajouterTypeTicket(pass);
        new Avantage(vip, parking, 1).connecter();
        parking.setLimite(true);

        assertThrows(FestivalException.class, () -> festival.modifierTypeTicket(vip, "Pass", 10, 60, 4), "Nom déjà utilisé");
        assertThrows(FestivalException.class, () -> festival.modifierTypeTicket(vip, "VIP Gold", 500, 60, 4), "500 places de parking pour un stock limité à 100");
        assertEquals("VIP", vip.getType(), "Une modification refusée ne doit rien changer");
        assertEquals(0, vip.getQuantite());
        assertEquals(50, vip.getPrix());

        festival.modifierTypeTicket(vip, "VIP Gold", 80, 60, 4);
        assertEquals("VIP Gold", vip.getType());
        assertEquals(80, vip.getQuantite());
        assertEquals(4, vip.getSurface());
    }

    @Test
    @DisplayName("Renommer un stock garde les noms uniques")
    void renameStockKeepsNamesUnique() throws Exception {
        Stock gobelet = new Stock("Gobelet", 10, false, 1);
        festival.ajouterStock(gobelet);
        assertThrows(FestivalException.class, () -> festival.renommerStock(gobelet, "Place de parking"));
        festival.renommerStock(gobelet, "Gobelet réutilisable");
        assertEquals("Gobelet réutilisable", gobelet.getNom());
    }

    @Test
    @DisplayName("Une surface par ticket doit être positive, un stock peut être gratuit")
    void surfaceAndFreeStock() {
        assertThrows(TypeTicketException.class, () -> vip.setSurface(0));
        assertThrows(TypeTicketException.class, () -> vip.setSurface(Float.NaN));
        assertDoesNotThrow(() -> parking.setPrix(0));
    }
}
