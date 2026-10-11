package com.musigma.controllers.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.musigma.controllers.FxTestSupport.onFxThread;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Régressions des champs de saisie numériques (AUDIT.md C5 à C7).
 * Nécessite un affichage pour démarrer JavaFX, sinon les tests sont ignorés.
 */
@DisplayName("Champs numériques")
class NumberTextFieldTest {

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

    @Test
    @DisplayName("C7 — une valeur collée (sans frappe clavier) est transmise au modèle")
    void bindValueFollowsTextChanges() throws Exception {
        AtomicReference<Integer> received = new AtomicReference<>();
        IntTextField field = onFxThread(IntTextField::new);
        onFxThread(() -> {
            field.bindValue("Erreur", 5, received::set);
            return null;
        });
        assertNull(received.get(), "La valeur initiale ne doit pas être renvoyée au modèle");
        onFxThread(() -> {
            field.setText("42");
            return null;
        });
        assertEquals(42, received.get());
    }

    @Test
    @DisplayName("FloatTextField accepte la virgule comme séparateur décimal")
    void floatAcceptsComma() throws Exception {
        FloatTextField field = onFxThread(FloatTextField::new);
        assertEquals(12.5f, onFxThread(() -> {
            field.setText("12,5");
            return field.getValue();
        }));
        assertEquals("0.1", onFxThread(() -> {
            field.setValue(0.1f);
            return field.getText();
        }));
    }

    @Test
    @DisplayName("Les contraintes positive et non-nulle sont vérifiées")
    void positiveAndNotNull() throws Exception {
        IntTextField field = onFxThread(() -> new IntTextField(true, true));
        assertFalse(onFxThread(() -> {
            field.setText("-3");
            return field.isValid();
        }), "Valeur négative refusée");
        assertFalse(onFxThread(() -> {
            field.setText("0");
            return field.isValid();
        }), "Valeur nulle refusée");
        assertFalse(onFxThread(() -> {
            field.setText("99999999999");
            return field.isValid();
        }), "Entier trop grand refusé");
        assertTrue(onFxThread(() -> {
            field.setText("3");
            return field.isValid();
        }));
    }
}
