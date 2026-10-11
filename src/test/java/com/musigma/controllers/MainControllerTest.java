package com.musigma.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Régressions de la fenêtre principale (AUDIT.md A2).
 */
@DisplayName("Fenêtre principale")
class MainControllerTest {

    @Test
    @DisplayName("A2 — le guide est extrait des ressources, donc lisible aussi depuis le jar")
    void extractGuide() throws Exception {
        Path guide = MainController.extractResource("/com/musigma/guide/guide.pdf", ".pdf");
        assertTrue(Files.size(guide) > 0, "Le guide extrait ne doit pas être vide");
        assertEquals("%PDF", new String(Files.readAllBytes(guide), 0, 4), "Le fichier extrait doit être un PDF");
        Files.delete(guide);
    }

    @Test
    @DisplayName("A2 — une ressource absente est signalée")
    void missingResource() {
        assertThrows(FileNotFoundException.class, () -> MainController.extractResource("/absent.pdf", ".pdf"));
    }
}
