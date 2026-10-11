package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dates, scènes et bilan du festival (UC1, UC3.2, UC3.3, AUDIT.md M5).
 */
@DisplayName("Dates, scènes et bilan")
class FestivalScheduleTest {

    Festival festival;
    Artiste artiste;
    LocalDateTime start;

    @BeforeEach
    void setUp() throws FestivalException, ArtisteException {
        start = LocalDate.now().plusDays(1).atStartOfDay();
        festival = new Festival("Festival", start, 1000, 5000, "Grenoble");
        artiste = new Artiste("Gorillaz", "Pop", 20000);
        festival.ajouterArtiste(artiste);
    }

    @Test
    @DisplayName("Un nouveau festival dure un jour et compte une scène")
    void defaults() {
        assertEquals(start.plusDays(1), festival.getFin());
        assertEquals(24 * 60, festival.getDureeMinutes());
        assertEquals(List.of(Festival.SCENE_PAR_DEFAUT), festival.getScenes());
    }

    @Test
    @DisplayName("La date de fin suit la date de début et couvre les représentations")
    void setFin() throws FestivalException, RepresentationException {
        assertThrows(FestivalException.class, () -> festival.setFin(start), "La fin doit suivre le début");
        assertThrows(FestivalException.class, () -> festival.setFin(null));
        festival.setFin(start.plusDays(3));
        festival.ajouterRepresentation(new Representation(2 * 24 * 60, 60, Festival.SCENE_PAR_DEFAUT, artiste));
        assertThrows(FestivalException.class, () -> festival.setFin(start.plusDays(2)), "La représentation du 3e jour serait hors du festival");
    }

    @Test
    @DisplayName("Déplacer le début déplace aussi la fin")
    void setStartKeepsDuration() throws FestivalException {
        festival.setFin(start.plusDays(3));
        festival.setDebut(start.plusDays(7));
        assertEquals(start.plusDays(10), festival.getFin());
    }

    @Test
    @DisplayName("Une représentation doit être sur une scène du festival et pendant le festival")
    void representationSlot() throws RepresentationException {
        assertThrows(FestivalException.class, () -> festival.ajouterRepresentation(new Representation(600, 60, "Scène inconnue", artiste)));
        assertThrows(FestivalException.class, () -> festival.ajouterRepresentation(new Representation(24 * 60 - 30, 60, Festival.SCENE_PAR_DEFAUT, artiste)), "Déborde après la fin du festival");
    }

    @Test
    @DisplayName("Ajouter, renommer et supprimer des scènes")
    void scenes() throws FestivalException, RepresentationException {
        festival.ajouterScene("Chapiteau");
        assertThrows(FestivalException.class, () -> festival.ajouterScene("chapiteau "), "Nom déjà utilisé");
        assertThrows(FestivalException.class, () -> festival.ajouterScene(" "), "Nom vide");

        Representation representation = new Representation(600, 60, "Chapiteau", artiste);
        festival.ajouterRepresentation(representation);
        festival.renommerScene("Chapiteau", "Grand chapiteau");
        assertEquals("Grand chapiteau", representation.getScene(), "Les représentations suivent le renommage");

        assertThrows(FestivalException.class, () -> festival.supprimerScene("Grand chapiteau"), "La scène accueille une représentation");
        festival.supprimerRepresentation(representation);
        festival.supprimerScene("Grand chapiteau");
        assertThrows(FestivalException.class, () -> festival.supprimerScene(Festival.SCENE_PAR_DEFAUT), "Le festival garde au moins une scène");
    }

    @Test
    @DisplayName("Le temps libre du planning tient compte des scènes et des représentations")
    void freeMinutes() throws FestivalException, RepresentationException {
        festival.ajouterScene("Chapiteau");
        festival.ajouterRepresentation(new Representation(600, 90, "Chapiteau", artiste));
        assertEquals(2L * 24 * 60 - 90, festival.getMinutesLibres());
    }

    @Test
    @DisplayName("UC3.2 — le bilan regroupe tickets, location, cachets et stocks nécessaires")
    void budget() throws FestivalException, StockException, TypeTicketException, AvantageException {
        Stock tshirt = new Stock("T-shirt", 50, false, 8);
        festival.ajouterStock(tshirt);
        TypeTicket vip = new TypeTicket("VIP", 100, 60);
        festival.ajouterTypeTicket(vip);
        new Avantage(vip, tshirt, 1).connecter();

        assertEquals(50, tshirt.getQuantiteManquante(), "100 tickets avec un T-shirt chacun pour 50 T-shirts prévus");
        assertEquals(100, tshirt.getQuantiteNecessaire());

        Bilan budget = festival.getBilan();
        assertEquals(6000, budget.totalRecettes(), 1e-6);
        assertEquals(1000 + 20000 + 100 * 8, budget.totalCharges(), 1e-6, "Location + cachet + T-shirts nécessaires");
        assertEquals(6000 - 21800, budget.resultat(), 1e-6);
    }
}
