package com.musigma.models;

import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.RepresentationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepresentationTest {

    Artiste artiste;
    Representation representation;

    @BeforeEach
    void setUp() throws ArtisteException {
        artiste = new Artiste("DCAC", "rock", 100f);
        representation = new Representation(1, 1, "Scene Name", artiste);
    }

    @Test
    void setArtiste() throws ArtisteException {
        assertThrows(RepresentationException.class, () -> representation.setArtiste(null), "Artiste null, doit être défini");
        Artiste newArtiste = new Artiste("Test Test", "rock", 150f);
        assertDoesNotThrow(() -> representation.setArtiste(newArtiste), "Artiste valide doit être défini sans exception");
        assertEquals(newArtiste, representation.getArtiste(), "L'artiste doit être changé pour le nouveau valide");
    }

    @Test
    void setStartDelta() {
        assertDoesNotThrow(() -> representation.setStartDelta(200), "La différence de temps doit pouvoir être changée sans exception");
        assertEquals(200, representation.getStartDelta(), "La différence de temps doit être changée pour le nouveau valide");
        assertThrows(RepresentationException.class, () -> representation.setStartDelta(-100), "La différence de temps ne doit pas pouvoir être négative");
    }

    @Test
    void setDuration() {
        assertDoesNotThrow(() -> representation.setDuration(120), "La durée doit pouvoir être changée sans exception");
        assertEquals(120, representation.getDuration(), "La durée doit être changée pour le nouveau valide");
        assertThrows(RepresentationException.class, () -> representation.setDuration(0), "La durée ne doit pas pouvoir être nulle");
        assertThrows(RepresentationException.class, () -> representation.setDuration(-30), "La durée ne doit pas pouvoir être négative");
    }

    @Test
    void setScene() {
        assertThrows(RepresentationException.class, () -> representation.setScene(null), "La scène ne doit pas pouvoir être null");
        assertThrows(RepresentationException.class, () -> representation.setScene(""), "La scène ne doit pas pouvoir être vide");
        assertDoesNotThrow(() -> representation.setScene("Scene Test"), "La scène valide doit pouvoir être changée sans exception");
        assertEquals("Scene Test", representation.getScene(), "La scène doit être changée pour la nouvelle valide");
    }

    @Test
    void compareTo() {
        Representation otherRepresentation = new Representation(5, 2, "Scene Name", artiste);
        assertTrue(representation.compareTo(otherRepresentation) < 0, "La représentation actuelle doit être antérieure à l'autre représentation");

        otherRepresentation = new Representation(1, 2, "Scene Name", artiste);
        assertEquals(0, representation.compareTo(otherRepresentation), "Les représentations doivent avoir la même date de début");

        otherRepresentation = new Representation(0, 2, "Scene Name", artiste);
        assertTrue(representation.compareTo(otherRepresentation) > 0, "La représentation actuelle doit être postérieure à l'autre représentation");
    }
}
