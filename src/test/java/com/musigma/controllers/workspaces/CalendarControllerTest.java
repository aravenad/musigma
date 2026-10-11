package com.musigma.controllers.workspaces;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.musigma.controllers.Dialogs;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.musigma.controllers.FxTestSupport.onFxThread;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Synchronisation entre le planning CalendarFX et le modèle (UC4.2, AUDIT.md C1 à C4).
 * Nécessite un affichage pour démarrer JavaFX, sinon les tests sont ignorés.
 */
@DisplayName("Planning")
class CalendarControllerTest {

    Festival festival;
    Artiste artiste;
    Representation first, second;
    CalendarController controller;
    List<String> errors;

    @BeforeEach
    void setUp() throws Exception {
        LocalDateTime start = LocalDate.now().plusDays(1).atStartOfDay();
        festival = new Festival("Festival", start, 0, 1000, "Grenoble");
        festival.setFin(start.plusDays(2));
        festival.ajouterScene("Chapiteau");
        artiste = new Artiste("Gorillaz", "Pop", 1000);
        festival.ajouterArtiste(artiste);
        first = new Representation(20 * 60, 60, Festival.SCENE_PAR_DEFAUT, artiste);   // 20h-21h
        second = new Representation(22 * 60, 60, Festival.SCENE_PAR_DEFAUT, artiste);  // 22h-23h
        festival.ajouterRepresentation(first);
        festival.ajouterRepresentation(second);

        errors = new ArrayList<>();
        Dialogs.setErrorDisplay(errors::add);
        controller = onFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(CalendarController.class.getResource(CalendarController.REGISTER.viewPath));
            loader.load();
            CalendarController c = loader.getController();
            c.initialize(festival);
            return c;
        });
    }

    @AfterEach
    void tearDown() {
        Dialogs.setErrorDisplay(null);
    }

    @SuppressWarnings("unchecked")
    private Calendar<Representation> calendar(String scene) {
        return (Calendar<Representation>) controller.calendarView.getCalendarSources().get(0).getCalendars().stream()
                .filter(c -> c.getName().equals(scene))
                .findFirst()
                .orElseThrow();
    }

    private List<Entry<?>> entries(String scene) {
        List<Entry<?>> entries = new ArrayList<>();
        calendar(scene).findEntries(festival.getDebut().toLocalDate(), festival.getFin().toLocalDate(), ZoneId.systemDefault())
                .values().forEach(entries::addAll);
        return entries;
    }

    @SuppressWarnings("unchecked")
    private Entry<Representation> entryOf(Representation representation) {
        return (Entry<Representation>) entries(representation.getScene()).stream()
                .filter(e -> e.getUserObject() == representation)
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("C1 — chaque scène est un calendrier et chaque représentation y figure à sa date réelle")
    void entriesMatchModel() throws Exception {
        onFxThread(() -> {
            assertEquals(List.of(Festival.SCENE_PAR_DEFAUT, "Chapiteau"),
                    controller.calendarView.getCalendarSources().get(0).getCalendars().stream().map(Calendar::getName).toList());
            assertEquals(2, entries(Festival.SCENE_PAR_DEFAUT).size());
            Entry<Representation> entry = entryOf(first);
            assertEquals(festival.getDebut().plusHours(20), entry.getStartAsLocalDateTime());
            assertEquals(festival.getDebut().plusHours(21), entry.getEndAsLocalDateTime());
            assertEquals("Gorillaz", entry.getTitle());
            return null;
        });
    }

    @Test
    @DisplayName("La semaine affichée commence le premier jour du festival")
    void weekStartsOnFestivalFirstDay() throws Exception {
        assertEquals(festival.getDebut().getDayOfWeek(), onFxThread(() -> controller.calendarView.getWeekFields().getFirstDayOfWeek()));
    }

    @Test
    @DisplayName("C3 — déplacer une entrée déplace la représentation")
    void moveEntryUpdatesModel() throws Exception {
        onFxThread(() -> {
            Entry<Representation> entry = entryOf(first);
            entry.setInterval(festival.getDebut().plusDays(1).plusHours(18), festival.getDebut().plusDays(1).plusHours(19).plusMinutes(30));
            return null;
        });
        assertEquals(List.of(), errors);
        assertEquals(24 * 60 + 18 * 60, first.getDecalageDebut());
        assertEquals(90, first.getDuree());
    }

    @Test
    @DisplayName("C2 — un déplacement en collision est refusé, signalé, et l'entrée revient à sa place")
    void collidingMoveIsReverted() throws Exception {
        Entry<Representation> entry = onFxThread(() -> {
            Entry<Representation> e = entryOf(first);
            e.setInterval(festival.getDebut().plusHours(21).plusMinutes(30), festival.getDebut().plusHours(22).plusMinutes(30));
            return e;
        });
        assertEquals(1, errors.size(), "La collision avec la représentation de 22h doit être signalée");
        assertEquals(20 * 60, first.getDecalageDebut(), "Le modèle ne doit pas changer");
        assertEquals(festival.getDebut().plusHours(20), onFxThread(entry::getStartAsLocalDateTime), "L'entrée doit revenir à 20h");
    }

    @Test
    @DisplayName("C3 — supprimer une entrée du calendrier supprime la représentation")
    void removeEntryRemovesRepresentation() throws Exception {
        onFxThread(() -> {
            entryOf(second).removeFromCalendar();
            return null;
        });
        assertFalse(festival.getRepresentations().contains(second));
        assertFalse(artiste.getRepresentations().contains(second));
    }

    @Test
    @DisplayName("C3 — changer une entrée de calendrier change la scène de la représentation")
    void changeCalendarChangesScene() throws Exception {
        onFxThread(() -> {
            entryOf(second).setCalendar(calendar("Chapiteau"));
            return null;
        });
        assertEquals("Chapiteau", second.getScene());
    }
}
