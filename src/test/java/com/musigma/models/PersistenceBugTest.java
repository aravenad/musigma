package com.musigma.models;

import com.musigma.models.exception.FestivalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Défauts connus de la persistance et des validations transverses (AUDIT.md §3.2 et §3.4).
 * Ces tests échouent tant que les défauts ne sont pas corrigés : lancer avec {@code mvn test -Pbugs}.
 */
@Tag("bug")
@DisplayName("Défauts connus — persistance")
class PersistenceBugTest {

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {Festival.class, Artiste.class, Representation.class, TypeTicket.class, Stock.class, Avantage.class})
    @DisplayName("M16 — chaque classe sérialisée déclare un serialVersionUID")
    void declaresSerialVersionUID(Class<?> klass) {
        var field = assertDoesNotThrow(() -> klass.getDeclaredField("serialVersionUID"),
                klass.getSimpleName() + " doit fixer son serialVersionUID pour rester lisible après recompilation");
        assertTrue(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()));
    }

    @Test
    @DisplayName("M7 — une date de début null est refusée par une FestivalException")
    void nullStartDate() throws FestivalException {
        Festival festival = new Festival("Festival", LocalDateTime.now().plusDays(1), 0, 1000, "Grenoble");
        assertThrows(FestivalException.class, () -> festival.setStart(null));
    }
}
