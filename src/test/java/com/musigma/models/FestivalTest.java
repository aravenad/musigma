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
        stock = new Stock("Bouteille Coca", 100, true, 2.5);
        artiste = new Artiste("DCAC", "rock", 100f);
        ticketType = new TypeTicket("VIP", 10, 60);
        avantage = new Avantage(ticketType, stock, 2);
    }

    @Test
    void setFile() {
        File file = new File("Test File");
        assertDoesNotThrow(() -> festival.setFile(file), "Le festival doit pouvoir pouvoir de changer de fichier avec un valide");
        assertEquals(file, festival.getFile(), "Le festival doit avoir changé son fichier par le nouveau nom valide");
        assertThrows(FestivalException.class, () -> festival.setFile(null), "Le festival ne doit pas changé de fichier par un invalide car null");
    }

    @Test
    void setName() {
        assertDoesNotThrow(() -> festival.setName("New Name"), "Le festival doit pouvoir être renommé par un nom valide");
        assertEquals("New Name", festival.getName(), "Le festival doit être renommé par le nouveau nom valide");
        assertThrows(FestivalException.class, () -> festival.setName(null), "Le festival ne doit pas être nommé par un nom invalide car null");
        assertThrows(FestivalException.class, () -> festival.setName(""), "Le festival ne doit pas être nommé par un nom invalide car vide");
    }

    @Test
    void setStart() {
        LocalDateTime newStart = LocalDateTime.now().plusDays(5);
        assertDoesNotThrow(() -> festival.setStart(newStart), "Le festival doit pouvoir être déplacé a une nouvelle date valide");
        assertEquals(newStart, festival.getStart(), "Le festival doit être déplacé à la nouvelle date valide");

        LocalDateTime pastStart = LocalDateTime.now().minusDays(1);
        assertThrows(FestivalException.class, () -> festival.setStart(pastStart), "Le festival ne doit pas pouvoir être déplacé a une nouvelle date invalide car antérieur à la date d'aujourd'hui");
    }

    @Test
    void setLocationPrice() {
        assertDoesNotThrow(() -> festival.setLocationPrice(200), "Le prix de location de l'emplacement du festival doit pouvoir être changé par un nouveau valide");
        assertEquals(200, festival.getLocationPrice(), "Le prix de location de l'emplacement du festival doit être changé par le nouveau valide");
        assertThrows(FestivalException.class, () -> festival.setLocationPrice(-100), "Le prix de location de l'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car négatif");
    }

    @Test
    void setArea() {
        assertDoesNotThrow(() -> festival.setArea(100), "L'aire du festival doit pouvoir être changé par une nouvelle valide");
        assertEquals(100, festival.getArea(), "L'aire du festival doit être changé par la nouvelle valide");
        assertThrows(FestivalException.class, () -> festival.setArea(-500), "L'aire du festival ne doit pas pouvoir être changé par une nouvelle invalide car négative");
        assertThrows(FestivalException.class, () -> festival.setArea(0), "L'aire du festival ne doit pas pouvoir être changé par une nouvelle invalide car nulle (0)");
    }

    @Test
    void setLocation() {
        assertDoesNotThrow(() -> festival.setLocation("Lyon"), "L'emplacement du festival doit pouvoir être changé par un nouveau valide");
        assertEquals("Lyon", festival.getLocation(), "L'emplacement du festival doit être changé par le nouveau valide");
        assertThrows(FestivalException.class, () -> festival.setLocation(null), "L'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car null");
        assertThrows(FestivalException.class, () -> festival.setLocation(""), "L'emplacement du festival ne doit pas pouvoir être changé par un nouveau invalide car vide");
    }

    @Test
    void addArtiste() throws ArtisteException {
        assertDoesNotThrow(() -> festival.addArtiste(artiste), "Le festival doit pouvoir ajouter un nouvel artiste");
        assertTrue(festival.getArtistes().contains(artiste), "Le festival doit avoir ajouté le nouvel artiste");
        assertThrows(FestivalException.class, () -> festival.addArtiste(null), "Le festival ne doit pas pouvoir ajouter null comme artiste");
    }

    @Test
    void removeArtiste() throws ArtisteException, FestivalException {
        festival.addArtiste(artiste);
        assertDoesNotThrow(() -> festival.removeArtiste(artiste), "Le festival doit pouvoir retirer un artiste de ceux y participant");
        assertFalse(festival.getArtistes().contains(artiste), "Le festival ne doit plus avoir l'artiste retirer");
        assertThrows(FestivalException.class, () -> festival.removeArtiste(artiste), "Le festival ne doit pas pouvoir retirer un artiste s'il ne participa pas au festival");
    }

    private Representation newRepresentation(int hour, int duration) {
        return new Representation(hour * 60, duration, "Main Stage", artiste);
    }

    @Test
    void addRepresentation() {
        Representation representation = newRepresentation(16, 120);
        assertDoesNotThrow(() -> festival.addRepresentation(representation), "Le festival doit pouvoir ajouter une nouvelle représentation");
        assertTrue(festival.getRepresentations().contains(representation), "Le festival doit avoir ajouter la nouvelle représentation");

        Representation representation1 = newRepresentation(13, 120);
        assertDoesNotThrow(() -> festival.addRepresentation(representation1), "Le festival doit pouvoir ajouter une nouvelle représentation valide car sans collision");
        assertTrue(festival.getRepresentations().contains(representation1), "Le festival doit avoir ajouter la nouvelle représentation valide car sans collision");

        Representation representation2 = newRepresentation(14, 60);
        assertThrows(FestivalException.class, () -> festival.addRepresentation(representation2), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (après)");
        assertFalse(festival.getRepresentations().contains(representation2), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (après)");

        Representation representation3 = newRepresentation(12, 90);
        assertThrows(FestivalException.class, () -> festival.addRepresentation(representation3), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (avant)");
        assertFalse(festival.getRepresentations().contains(representation3), "Le festival ne doit pas pouvoir ajouter une nouvelle représentation invalide car en collision (avant)");

        assertThrows(FestivalException.class, () -> festival.addRepresentation(null), "Le festival ne doit pas pouvoir ajouter null comme représentation");
    }

    @Test
    void removeRepresentation() throws FestivalException {
        Representation representation = newRepresentation(16, 120);
        festival.addRepresentation(representation);
        assertDoesNotThrow(() -> festival.removeRepresentation(representation), "Le festival doit pouvoir retirer une de ces représentations");
        assertFalse(festival.getRepresentations().contains(representation), "Le festival doit avoir retiré la représentation");
        assertThrows(FestivalException.class, () -> festival.removeRepresentation(newRepresentation(1, 10)), "Le festival ne doit pas pouvoir retirer une representation n'étant pas parmis les siennes");
    }

    @Test
    void addTicketType() throws TypeTicketException {
        assertDoesNotThrow(() -> festival.addTicketType(ticketType), "Le festival doit pouvoir ajouter un nouveau type de ticket");
        assertTrue(festival.getTicketTypes().contains(ticketType), "Le festival doit avoir ajouté le nouveau type de ticket");
        assertThrows(FestivalException.class, () -> festival.addTicketType(null), "Le festival ne doit pas pouvoir ajouter null comme type de ticket");
    }

    @Test
    void removeTicketType() throws TypeTicketException, FestivalException {
        festival.addTicketType(ticketType);
        assertDoesNotThrow(() -> festival.removeTicketType(ticketType), "Le festival doit pouvoir retirer un de ses types de ticket");
        assertFalse(festival.getTicketTypes().contains(ticketType), "Le festival doit avoir retiré le type de ticket");
        assertThrows(FestivalException.class, () -> festival.removeTicketType(ticketType), "Le festival ne doit pas pouvoir retirer un types de tickets n'étant pas des siens");
    }

    @Test
    void addStock() throws StockException {
        assertDoesNotThrow(() -> festival.addStock(stock), "Le festival doit pouvoir ajouter un nouveau stock");
        assertTrue(festival.getStocks().contains(stock), "Le festival doit avoir ajouté le nouveau stock");
        assertThrows(FestivalException.class, () -> festival.addStock(null), "Le festival ne doit pas pouvoir ajouter null comme stock");
    }

    @Test
    void removeStock() throws StockException, FestivalException {
        festival.addStock(stock);
        assertDoesNotThrow(() -> festival.removeStock(stock), "Le festival doit pouvoir retirer un de ses stocks");
        assertFalse(festival.getStocks().contains(stock), "Le festival doit avoir retiré le stock");
        assertThrows(FestivalException.class, () -> festival.removeStock(stock), "Le festival ne doit pas pouvoir retirer un stock n'étant des siens");
    }

    @Test
    void save() throws FestivalException {
        festival.addArtiste(artiste);
        festival.addTicketType(ticketType);
        festival.addRepresentation(newRepresentation(1, 1));
        File testFile = new File(String.format("%s/test1.bdd", System.getProperty("java.io.tmpdir")));
        assertThrows(FestivalException.class, () -> festival.save(), "Le festival ne doit pas pouvoir se sauvegarder sans fichier");
        festival.setFile(testFile);
        assertDoesNotThrow(() -> festival.save(), "Le festival doit pouvoir se sauvegarder avec un fichier");
    }

    @Test
    void festival() throws FestivalException {
        festival.addArtiste(artiste);
        festival.addTicketType(ticketType);
        festival.addRepresentation(newRepresentation(1, 1));
        File testFile = new File(String.format("%s/test2.bdd", System.getProperty("java.io.tmpdir")));
        festival.setFile(testFile);
        festival.save();

        Festival clonedFestival = Festival.Festival(testFile);
        assertEquals(festival.hashCode(), clonedFestival.hashCode(), "Le festival doit conserver son hashcode");
        assertEquals(festival, clonedFestival, "Le festival doit conserver les informations");
    }

    @Test
    void optimizeResult() throws FestivalException, StockException, TypeTicketException, AvantageException {
        festival = new Festival("Valid Festival", LocalDateTime.now().plusDays(1), 100, 14000, "Paris");
        Stock gobelet = new Stock("Gobelet", 8000, true, 2.5);
        Stock parking = new Stock("Place de parking", 3000, true, 5);

        TypeTicket vip = new TypeTicket("VIP", 0, 50);
        new Avantage(vip, gobelet, 1);
        new Avantage(vip, parking, 1);


        TypeTicket nonVip = new TypeTicket("Non-VIP", 0, 35);
        new Avantage(nonVip, gobelet, 2);

        System.out.println(festival.optimizeResult());
    }
}
