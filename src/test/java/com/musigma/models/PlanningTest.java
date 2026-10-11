package com.musigma.models;

import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.RepresentationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Régressions du planning (AUDIT.md §3.2), UC4.1 et UC4.2.
 */
@DisplayName("Planning")
class PlanningTest {

    Festival festival;
    Artiste artiste;

    @BeforeEach
    void setUp() throws FestivalException, ArtisteException {
        festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 1000, "Grenoble");
        festival.addScene("Scène 1");
        festival.addScene("Scène 2");
        artiste = new Artiste("Gorillaz", "Pop", 1000);
        festival.addArtiste(artiste);
    }

    @Test
    @DisplayName("M1 — deux représentations simultanées sur deux scènes différentes sont conservées")
    void simultaneousRepresentationsOnDifferentScenes() throws FestivalException, RepresentationException {
        festival.addRepresentation(new Representation(600, 60, "Scène 1", artiste));
        festival.addRepresentation(new Representation(600, 60, "Scène 2", artiste));
        assertEquals(2, festival.getRepresentations().size(), "Une représentation simultanée sur une autre scène ne doit pas être perdue");
    }

    @Test
    @DisplayName("M2 — une collision sur la même scène est détectée même si une autre scène s'intercale")
    void collisionHiddenByAnotherScene() throws FestivalException, RepresentationException {
        festival.addRepresentation(new Representation(600, 120, "Scène 1", artiste)); // 10h00-12h00
        festival.addRepresentation(new Representation(660, 30, "Scène 2", artiste));  // 11h00-11h30
        Representation colliding = new Representation(690, 60, "Scène 1", artiste);   // 11h30-12h30
        assertThrows(FestivalException.class, () -> festival.addRepresentation(colliding), "La représentation chevauche la première sur la Scène 1");
        assertFalse(festival.getRepresentations().contains(colliding));
    }

    @Test
    @DisplayName("M3 — relier un artiste et sa représentation ne provoque pas de récursion infinie")
    void artisteRepresentationCycle() throws RepresentationException {
        Representation representation = new Representation(600, 60, "Scène 1", artiste);
        assertDoesNotThrow(() -> {
            festival.addRepresentation(representation);
            artiste.hashCode();
            representation.hashCode();
            artiste.equals(new Artiste("Gorillaz", "Pop", 1000));
            festival.isModified();
        });
    }

    @Test
    @DisplayName("Déplacer une représentation vérifie que la nouvelle scène est libre")
    void updateRepresentationChecksCollision() throws FestivalException, RepresentationException {
        Representation first = new Representation(600, 60, "Scène 1", artiste);
        Representation second = new Representation(600, 60, "Scène 2", artiste);
        festival.addRepresentation(first);
        festival.addRepresentation(second);

        assertThrows(FestivalException.class, () -> festival.updateRepresentation(second, 630, 60, "Scène 1"), "Le créneau est occupé par la première représentation");
        assertEquals("Scène 2", second.getScene(), "Un déplacement refusé ne doit rien modifier");
        assertThrows(RepresentationException.class, () -> festival.updateRepresentation(second, 700, -5, "Scène 1"), "Durée invalide");
        assertDoesNotThrow(() -> festival.updateRepresentation(second, 660, 60, "Scène 1"), "Le créneau suivant est libre");
        assertEquals(List.of(first, second), festival.getRepresentations(), "Les représentations sont triées par début");
        assertFalse(festival.isSceneFree("Scène 1", 650, 30));
        assertTrue(festival.isSceneFree("Scène 2", 650, 30));
    }

    @Test
    @DisplayName("M4 — le constructeur de Representation valide ses paramètres")
    void representationConstructorValidates() {
        assertAll(
                () -> assertThrows(RepresentationException.class, () -> new Representation(-50, 60, "Scène 1", artiste), "Début négatif"),
                () -> assertThrows(RepresentationException.class, () -> new Representation(600, 0, "Scène 1", artiste), "Durée nulle"),
                () -> assertThrows(RepresentationException.class, () -> new Representation(600, 60, " ", artiste), "Scène vide"),
                () -> assertThrows(RepresentationException.class, () -> new Representation(600, 60, "Scène 1", null), "Artiste null")
        );
    }

    @Test
    @DisplayName("M6 — planifier une représentation la rattache à son artiste")
    void representationLinkedToArtiste() throws FestivalException, RepresentationException {
        Representation representation = new Representation(600, 60, "Scène 1", artiste);
        festival.addRepresentation(representation);
        assertTrue(artiste.getRepresentations().contains(representation));
    }

    @Test
    @DisplayName("M6 — seul un artiste engagé au festival peut être planifié (UC4.1 avant UC4.2)")
    void representationRequiresEngagedArtiste() throws ArtisteException, RepresentationException {
        Artiste notEngaged = new Artiste("Inconnu", "Jazz", 10);
        assertThrows(FestivalException.class, () -> festival.addRepresentation(new Representation(600, 60, "Scène 1", notEngaged)));
    }

    @Test
    @DisplayName("M6 — retirer un artiste retire ses représentations")
    void removeArtisteRemovesRepresentations() throws FestivalException, RepresentationException {
        Representation representation = new Representation(600, 60, "Scène 1", artiste);
        festival.addRepresentation(representation);
        festival.removeArtiste(artiste);
        assertFalse(festival.getRepresentations().contains(representation));
    }
}
