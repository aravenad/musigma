package com.musigma.models;

import com.musigma.models.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FestivalTest {

    Festival festival;
    Avantage avantage;
    Stock stock;
    Artiste artiste;
    TypeTicket ticketType;

    @BeforeEach
    void setUp() throws FestivalException, StockException, ArtisteException, TypeTicketException, AvantageException {
        festival = new Festival("Valid Festival", LocalDateTime.now().plusDays(1), 100, 500, "Paris");
        festival.ajouterScene("Main Stage");
        stock = new Stock("Bouteille Coca", 100, true, 2.5);
        artiste = new Artiste("DCAC", "rock", 100f);
        ticketType = new TypeTicket("VIP", 10, 60);
        avantage = new Avantage(ticketType, stock, 2);
    }

    @Test
    void setFichier() {
        File file = new File("Test File");
        assertDoesNotThrow(() -> festival.setFichier(file), "Le festival doit pouvoir pouvoir de changer de fichier avec un valide");
        assertEquals(file, festival.getFichier(), "Le festival doit avoir changé son fichier par le nouveau nom valide");
        assertThrows(FestivalException.class, () -> festival.setFichier(null), "Le festival ne doit pas changé de fichier par un invalide car null");
    }

    @Test
    void setNom() {
        assertDoesNotThrow(() -> festival.setNom("New Name"), "Le festival doit pouvoir être renommé par un nom valide");
        assertEquals("New Name", festival.getNom(), "Le festival doit être renommé par le nouveau nom valide");
        assertThrows(FestivalException.class, () -> festival.setNom(null), "Le festival ne doit pas être nommé par un nom invalide car null");
        assertThrows(FestivalException.class, () -> festival.setNom(""), "Le festival ne doit pas être nommé par un nom invalide car vide");
    }

    @Test
    void setDebut() {
        LocalDateTime newStart = LocalDateTime.now().plusDays(5);
        assertDoesNotThrow(() -> festival.setDebut(newStart), "Le festival doit pouvoir être déplacé a une nouvelle date valide");
        assertEquals(newStart, festival.getDebut(), "Le festival doit être déplacé à la nouvelle date valide");

        LocalDateTime pastStart = LocalDateTime.now().minusDays(1);
        assertThrows(FestivalException.class, () -> festival.setDebut(pastStart), "Le festival ne doit pas pouvoir être déplacé a une nouvelle date invalide car antérieur à la date d'aujourd'hui");
    }

    @Test
    void setPrixLocation() {
        assertDoesNotThrow(() -> festival.setPrixLocation(200), "Le prix de location de l'emplacement du festival doit pouvoir être changé par un nouveau valide");
        assertEquals(200, festival.getPrixLocation(), "Le prix de location de l'emplacement du festival doit être changé par le nouveau valide");
        assertThrows(FestivalException.class, () -> festival.setPrixLocation(-100), "Le prix de location de l'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car négatif");
    }

    @Test
    void setSuperficie() {
        assertDoesNotThrow(() -> festival.setSuperficie(100), "L'aire du festival doit pouvoir être changé par une nouvelle valide");
        assertEquals(100, festival.getSuperficie(), "L'aire du festival doit être changé par la nouvelle valide");
        assertThrows(FestivalException.class, () -> festival.setSuperficie(-500), "L'aire du festival ne doit pas pouvoir être changé par une nouvelle invalide car négative");
        assertThrows(FestivalException.class, () -> festival.setSuperficie(0), "L'aire du festival ne doit pas pouvoir être changé par une nouvelle invalide car nulle (0)");
    }

    @Test
    void setEmplacement() {
        assertDoesNotThrow(() -> festival.setEmplacement("Lyon"), "L'emplacement du festival doit pouvoir être changé par un nouveau valide");
        assertEquals("Lyon", festival.getEmplacement(), "L'emplacement du festival doit être changé par le nouveau valide");
        assertThrows(FestivalException.class, () -> festival.setEmplacement(null), "L'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car null");
        assertThrows(FestivalException.class, () -> festival.setEmplacement(""), "L'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car vide");
    }

    @Test
    void ajouterArtiste() throws ArtisteException {
        assertDoesNotThrow(() -> festival.ajouterArtiste(artiste), "Le festival doit pouvoir ajouter un nouvel artiste");
        assertTrue(festival.getArtistes().contains(artiste), "Le festival doit avoir ajouté le nouvel artiste");
        assertThrows(FestivalException.class, () -> festival.ajouterArtiste(null), "Le festival ne doit pas pouvoir ajouter null comme artiste");
    }

    @Test
    void retirerArtiste() throws ArtisteException, FestivalException {
        festival.ajouterArtiste(artiste);
        assertDoesNotThrow(() -> festival.retirerArtiste(artiste), "Le festival doit pouvoir retirer un artiste de ceux y participant");
        assertFalse(festival.getArtistes().contains(artiste), "Le festival ne doit plus avoir l'artiste retirer");
        assertThrows(FestivalException.class, () -> festival.retirerArtiste(artiste), "Le festival ne doit pas pouvoir retirer un artiste s'il ne participa pas au festival");
    }

    private Representation newRepresentation(int hour, int duration) throws RepresentationException {
        return new Representation(hour * 60, duration, "Main Stage", artiste);
    }

    @Test
    void ajouterRepresentation() throws FestivalException, RepresentationException {
        festival.ajouterArtiste(artiste);
        Representation representation = newRepresentation(16, 120);
        assertDoesNotThrow(() -> festival.ajouterRepresentation(representation), "Le festival doit pouvoir ajouter une nouvelle représentation");
        assertTrue(festival.getRepresentations().contains(representation), "Le festival doit avoir ajouter la nouvelle représentation");

        Representation representation1 = newRepresentation(13, 120);
        assertDoesNotThrow(() -> festival.ajouterRepresentation(representation1), "Le festival doit pouvoir ajouter une nouvelle représentation valide car sans collision");
        assertTrue(festival.getRepresentations().contains(representation1), "Le festival doit avoir ajouter la nouvelle représentation valide car sans collision");

        Representation representation2 = newRepresentation(14, 60);
        assertThrows(FestivalException.class, () -> festival.ajouterRepresentation(representation2), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (après)");
        assertFalse(festival.getRepresentations().contains(representation2), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (après)");

        Representation representation3 = newRepresentation(12, 90);
        assertThrows(FestivalException.class, () -> festival.ajouterRepresentation(representation3), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (avant)");
        assertFalse(festival.getRepresentations().contains(representation3), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (avant)");

        assertThrows(FestivalException.class, () -> festival.ajouterRepresentation(null), "Le festival ne doit pas pouvoir ajouter null comme représentation");
    }

    @Test
    void supprimerRepresentation() throws FestivalException, RepresentationException {
        festival.ajouterArtiste(artiste);
        Representation representation = newRepresentation(16, 120);
        festival.ajouterRepresentation(representation);
        assertDoesNotThrow(() -> festival.supprimerRepresentation(representation), "Le festival doit pouvoir retirer une de ces représentations");
        assertFalse(festival.getRepresentations().contains(representation), "Le festival doit avoir retiré la représentation");
        assertThrows(FestivalException.class, () -> festival.supprimerRepresentation(newRepresentation(1, 10)), "Le festival ne doit pas pouvoir retirer une representation n'étant pas parmis les siennes");
    }

    @Test
    void ajouterTypeTicket() throws TypeTicketException {
        assertDoesNotThrow(() -> festival.ajouterTypeTicket(ticketType), "Le festival doit pouvoir ajouter un nouveau type de ticket");
        assertTrue(festival.getTypesTicket().contains(ticketType), "Le festival doit avoir ajouté le nouveau type de ticket");
        assertThrows(FestivalException.class, () -> festival.ajouterTypeTicket(null), "Le festival ne doit pas pouvoir ajouter null comme type de ticket");
    }

    @Test
    void supprimerTypeTicket() throws TypeTicketException, FestivalException {
        festival.ajouterTypeTicket(ticketType);
        assertDoesNotThrow(() -> festival.supprimerTypeTicket(ticketType), "Le festival doit pouvoir retirer un de ses types de ticket");
        assertFalse(festival.getTypesTicket().contains(ticketType), "Le festival doit avoir retiré le type de ticket");
        assertThrows(FestivalException.class, () -> festival.supprimerTypeTicket(ticketType), "Le festival ne doit pas pouvoir retirer un types de tickets n'étant pas des siens");
    }

    @Test
    void ajouterStock() throws StockException {
        assertDoesNotThrow(() -> festival.ajouterStock(stock), "Le festival doit pouvoir ajouter un nouveau stock");
        assertTrue(festival.getStocks().contains(stock), "Le festival doit avoir ajouté le nouveau stock");
        assertThrows(FestivalException.class, () -> festival.ajouterStock(null), "Le festival ne doit pas pouvoir ajouter null comme stock");
    }

    @Test
    void supprimerStock() throws StockException, FestivalException {
        festival.ajouterStock(stock);
        assertDoesNotThrow(() -> festival.supprimerStock(stock), "Le festival doit pouvoir retirer un de ses stocks");
        assertFalse(festival.getStocks().contains(stock), "Le festival doit avoir retiré le stock");
        assertThrows(FestivalException.class, () -> festival.supprimerStock(stock), "Le festival ne doit pas pouvoir retirer un stock n'étant des siens");
    }

    @Test
    void enregistrer() throws FestivalException, RepresentationException {
        festival.ajouterArtiste(artiste);
        festival.ajouterTypeTicket(ticketType);
        festival.ajouterRepresentation(newRepresentation(1, 1));
        File testFile = new File(String.format("%s/test1.bdd", System.getProperty("java.io.tmpdir")));
        assertThrows(FestivalException.class, () -> festival.enregistrer(), "Le festival ne doit pas pouvoir se sauvegarder sans fichier");
        festival.setFichier(testFile);
        assertDoesNotThrow(() -> festival.enregistrer(), "Le festival doit pouvoir se sauvegarder avec un fichier");
    }

    @Test
    void chargerFestivalEnregistre() throws FestivalException, RepresentationException, AvantageException {
        festival.ajouterArtiste(artiste);
        festival.ajouterTypeTicket(ticketType);
        festival.ajouterStock(stock);
        avantage.connecter();
        festival.ajouterRepresentation(newRepresentation(1, 1));
        File testFile = new File(String.format("%s/test2.bdd", System.getProperty("java.io.tmpdir")));
        festival.setFichier(testFile);
        festival.enregistrer();

        Festival loaded = Festival.charger(testFile);
        assertEquals(festival.getNom(), loaded.getNom(), "Le festival doit conserver son nom");
        assertEquals(festival.getDebut(), loaded.getDebut(), "Le festival doit conserver sa date de début");
        assertEquals(festival.getEmplacement(), loaded.getEmplacement(), "Le festival doit conserver son emplacement");
        assertEquals(testFile, loaded.getFichier(), "Le festival chargé doit connaître son fichier");

        Artiste loadedArtiste = loaded.getArtistes().get(0);
        Representation loadedRepresentation = loaded.getRepresentations().get(0);
        assertSame(loadedArtiste, loadedRepresentation.getArtiste(), "La représentation doit rester liée à l'artiste chargé");
        assertTrue(loadedArtiste.getRepresentations().contains(loadedRepresentation), "L'artiste chargé doit connaître sa représentation");

        Avantage loadedAvantage = loaded.getTypesTicket().get(0).getAvantages().get(0);
        assertSame(loaded.getStocks().get(0), loadedAvantage.getStock(), "L'avantage doit rester lié au stock chargé");
        assertTrue(loaded.getStocks().get(0).getAvantages().contains(loadedAvantage), "Le stock chargé doit connaître l'avantage");
        assertFalse(loaded.estModifie(), "Un festival tout juste chargé n'est pas modifié");
    }

    @Test
    void estModifie() throws FestivalException, StockException {
        festival.ajouterStock(stock);
        festival.marquerEnregistre();
        assertFalse(festival.estModifie(), "Le festival ne doit pas être modifié juste après avoir été marqué enregistré");
        stock.setPrix(3);
        assertTrue(festival.estModifie(), "Modifier le prix d'un stock doit modifier le festival");
        stock.setPrix(2.5);
        assertFalse(festival.estModifie(), "Revenir à l'état enregistré ne doit plus compter comme une modification");
    }
}
