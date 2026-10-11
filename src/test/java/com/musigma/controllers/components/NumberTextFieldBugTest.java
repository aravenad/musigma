package com.musigma.controllers.components;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Défauts connus des champs de saisie numériques (AUDIT.md C5 à C7).
 * Nécessite un affichage pour démarrer JavaFX, sinon les tests sont ignorés.
 * Ces tests échouent tant que les défauts ne sont pas corrigés : lancer avec {@code mvn test -Pbugs}.
 */
@Tag("bug")
@DisplayName("Défauts connus — champs numériques")
class NumberTextFieldBugTest {

    private static boolean toolkitAvailable = true;

    @BeforeAll
    static void startToolkit() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException alreadyStarted) {
            // JavaFX est déjà démarré par un autre test
        } catch (Throwable noDisplay) {
            toolkitAvailable = false;
        }
    }

    /**
     * Exécute une action sur le thread JavaFX et renvoie son résultat.
     */
    private static <T> T onFxThread(Callable<T> action) throws Exception {
        assumeTrue(toolkitAvailable, "JavaFX ne peut pas démarrer sans affichage");
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("C5 — IntTextField affiche la valeur qu'on lui donne")
    void intSetValueRoundTrip() throws Exception {
        IntTextField field = onFxThread(IntTextField::new);
        assertEquals("5", onFxThread(() -> {
            field.setValue(5);
            return field.getText();
        }));
        assertEquals(1500, onFxThread(() -> {
            field.setValue(1500);
            return field.getValue();
        }));
    }

    @Test
    @DisplayName("C6 — FloatTextField conserve les grands nombres")
    void floatSetValueLargeNumber() throws Exception {
        FloatTextField field = onFxThread(FloatTextField::new);
        assertEquals(12_000_000f, onFxThread(() -> {
            field.setValue(12_000_000f);
            return field.getValue();
        }));
    }

    @Test
    @DisplayName("C7 — une saisie mal formée est signalée invalide sans lever d'exception")
    void malformedInputIsInvalid() throws Exception {
        FloatTextField field = onFxThread(FloatTextField::new);
        for (String input : new String[]{"(", "1.2.3", "--5", "+"}) {
            boolean valid = assertDoesNotThrow(() -> onFxThread(() -> {
                field.setText(input);
                return field.isValid();
            }), "Saisie « " + input + " »");
            assertFalse(valid, "Saisie « " + input + " » acceptée");
        }
    }
}
