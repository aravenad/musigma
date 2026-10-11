package com.musigma.models;

import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.RepresentationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ArtisteTest {

    Representation representation;
    Artiste artiste;

    @BeforeEach
    void setUp() throws ArtisteException, RepresentationException {
        artiste = new Artiste("Artiste Name", "Artiste Genre", 10000);
        representation = new Representation(60, 30, "Scene name", artiste);
    }

    @Test
    void setName() {
        assertDoesNotThrow(() -> artiste.setName("New Artiste Name"), "L'artiste doit pouvoir être renommé par un nom valide");
        assertEquals("New Artiste Name", artiste.getName(), "L'artiste doit être renommé par le nom valide");
        assertThrows(ArtisteException.class, () -> artiste.setName(null), "L'artiste ne doit pas pouvoir être renommé par un nom invalide car null");
        assertThrows(ArtisteException.class, () -> artiste.setName(""), "L'artiste ne doit pas pouvoir être renommé par un nom invalide car vide");
    }

    @Test
    void setGenre() {
        assertDoesNotThrow(() -> artiste.setGenre("New Artiste Genre"), "L'artiste doit pouvoir changé de genre par un genre valide");
        assertEquals("New Artiste Genre", artiste.getGenre(), "L'artiste doit avoir changé de genre par le genre valide");
        assertThrows(ArtisteException.class, () -> artiste.setGenre(null), "L'artiste ne doit pas pouvoir changé de genre par un genre invalide car null");
        assertThrows(ArtisteException.class, () -> artiste.setGenre(""), "L'artiste ne doit pas pouvoir changé de genre par un genre invalide car vide");
    }

    @Test
    void setPrice() {
        assertDoesNotThrow(() -> artiste.setPrice(500), "L'artiste doit pouvoir changer de cachet par un cachet valide");
        assertEquals(500, artiste.getPrice(), "L'artiste doit avoir changé de cachet par le cachet valide");
        assertDoesNotThrow(() -> artiste.setPrice(0), "L'artiste doit pouvoir avoir un cachet nul");
        assertThrows(ArtisteException.class, () -> artiste.setPrice(-1), "L'artiste ne doit pas pouvoir avoir un cachet négatif");
    }

    @Test
    void addRepresentation() {
        assertDoesNotThrow(() -> artiste.addRepresentation(representation), "Un artiste doit pouvoir ajouter une nouvelle représentation");
        assertTrue(artiste.getRepresentations().contains(representation), "L'artiste doit avoir ajouter la nouvelle représentation");
        assertThrows(ArtisteException.class, () -> artiste.addRepresentation(null), "Un artiste ne doit pas pouvoir ajouter null comme représentation");
    }

    @Test
    void addRepresentationOfAnotherArtiste() throws ArtisteException, RepresentationException {
        Representation other = new Representation(60, 30, "Scene name", new Artiste("Autre", "Jazz", 1));
        assertThrows(ArtisteException.class, () -> artiste.addRepresentation(other), "Un artiste ne doit pas pouvoir ajouter la représentation d'un autre artiste");
    }

    @Test
    void removeRepresentation() throws ArtisteException {
        artiste.addRepresentation(representation);
        assertDoesNotThrow(() -> artiste.removeRepresentation(representation), "Un artiste doit pouvoir retirer une de ces représentations");
        assertFalse(artiste.getRepresentations().contains(representation), "L'artiste doit avoir retiré la représentation");
        assertThrows(ArtisteException.class, () -> artiste.removeRepresentation(representation), "Un artiste ne doit pas pouvoir retirer une representation n'étant pas parmis les siennes");
    }
}