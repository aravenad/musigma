package com.musigma.controllers.workspaces;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.CustomValidField;
import com.musigma.controllers.components.IntTextField;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.musigma.controllers.Dialogs.*;

/**
 * Contrôleur pour l'espace de travail Planning (UC4.2).
 * <p>
 * Chaque scène du festival est un calendrier, chaque représentation une entrée.
 * Toute modification faite dans le calendrier (déplacement, durée, suppression) est d'abord
 * validée par le modèle ; si elle est refusée, l'entrée retrouve sa place et une erreur s'affiche.
 */
public class CalendarController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     * Définit le nom, l'icône et la vue de l'espace de travail.
     *
     * @see com.musigma.controllers.WorkspaceController
     */
    public static final WorkspaceController.WorkspaceRegister REGISTER = new WorkspaceController.WorkspaceRegister(
            "Planning",
            "/com/musigma/images/icons/calendar.png",
            "/com/musigma/views/calendar-view.fxml",
            "Choisissez un artiste, une scène, un jour, une heure et une durée, puis cliquez sur Planifier. Glissez une représentation pour la déplacer, tirez son bord pour changer sa durée, double-cliquez pour la modifier, clic droit pour la supprimer."
    );

    /**
     * Pas des heures proposées dans le formulaire, en minutes.
     */
    private static final int TIME_STEP = 15;

    @FXML
    CalendarView calendarView;

    @FXML
    ComboBox<Artiste> artisteChoice;

    @FXML
    ComboBox<String> sceneChoice;

    @FXML
    DatePicker dayChoice;

    @FXML
    ComboBox<LocalTime> timeChoice;

    @FXML
    IntTextField durationField;

    /**
     * Calendrier de chaque scène.
     */
    private final Map<String, Calendar<Representation>> calendars = new HashMap<>();

    /**
     * Vrai pendant que le contrôleur modifie lui-même les entrées, pour ignorer leurs écouteurs.
     */
    private boolean syncing = false;

    /**
     * Initialise le planning avec les scènes et les représentations du festival.
     *
     * @param festival le festival à gérer
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);

        CalendarSource source = new CalendarSource(festival.getNom());
        Calendar.Style[] styles = Calendar.Style.values();
        for (int i = 0; i < festival.getScenes().size(); i++) {
            String scene = festival.getScenes().get(i);
            Calendar<Representation> calendar = new Calendar<>(scene);
            calendar.setStyle(styles[i % styles.length]);
            calendars.put(scene, calendar);
            source.getCalendars().add(calendar);
        }
        calendarView.getCalendarSources().setAll(source);
        calendarView.setDefaultCalendarProvider(control -> calendars.get(festival.getScenes().get(0)));

        // Les représentations se créent par le formulaire, qui demande l'artiste et la scène
        calendarView.setEntryFactory(param -> null);
        calendarView.setContextMenuCallback(param -> null);
        calendarView.setEntryDetailsCallback(param -> {
            editRepresentation(param.getEntry());
            return true;
        });
        calendarView.setEntryContextMenuCallback(param -> entryContextMenu(param.getEntry()));
        // La semaine affichée commence le premier jour du festival : un festival d'au plus 7 jours tient
        // dans une seule semaine, où l'on peut glisser une représentation d'un jour à l'autre
        calendarView.setWeekFields(WeekFields.of(festival.getDebut().getDayOfWeek(), 1));
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowDeveloperConsole(false);
        calendarView.setShowSearchField(false);
        calendarView.setDate(festival.getDebut().toLocalDate());
        calendarView.showWeekPage();
        // Affiche d'abord l'heure de la première représentation, sinon le début d'après-midi
        calendarView.setRequestedTime(festival.getRepresentations().stream()
                .map(r -> festival.getDebut().plusMinutes(r.getDecalageDebut()).toLocalTime())
                .min(LocalTime::compareTo)
                .map(time -> time.minusHours(Math.min(1, time.getHour())))
                .orElse(LocalTime.of(14, 0)));

        for (Representation representation : festival.getRepresentations())
            addEntry(representation);

        setUpForm();
    }

    /**
     * Prépare le formulaire de planification : artistes engagés, scènes, jours du festival et heures.
     */
    private void setUpForm() {
        artisteChoice.getItems().setAll(festival.getArtistes());
        sceneChoice.getItems().setAll(festival.getScenes());
        sceneChoice.getSelectionModel().selectFirst();

        restrictToFestivalDays(dayChoice);
        dayChoice.setValue(festival.getDebut().toLocalDate());

        timeChoice.getItems().setAll(timeChoices());
        timeChoice.setValue(LocalTime.of(20, 0));
        durationField.setValue(60);
    }

    /**
     * Limite un sélecteur de date aux jours du festival.
     *
     * @param picker le sélecteur de date
     */
    private void restrictToFestivalDays(DatePicker picker) {
        LocalDate firstDay = festival.getDebut().toLocalDate();
        LocalDate lastDay = festival.getFin().minusNanos(1).toLocalDate();
        picker.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(firstDay) || date.isAfter(lastDay));
            }
        });
    }

    /**
     * Retourne les heures proposées dans les formulaires, de minuit à 23 h 45 par pas de {@value #TIME_STEP} minutes.
     *
     * @return les heures proposées
     */
    private static List<LocalTime> timeChoices() {
        List<LocalTime> times = new ArrayList<>();
        for (int minutes = 0; minutes < 24 * 60; minutes += TIME_STEP)
            times.add(LocalTime.MIDNIGHT.plusMinutes(minutes));
        return times;
    }

    /**
     * Planifie une représentation à partir du formulaire.
     */
    @FXML
    private void planRepresentation() {
        Artiste artiste = artisteChoice.getValue();
        String scene = sceneChoice.getValue();
        LocalDate day = dayChoice.getValue();
        LocalTime time = timeChoice.getValue();
        if (artiste == null) {
            showError(festival.getArtistes().isEmpty()
                    ? "Aucun artiste engagé : engagez d'abord un artiste dans l'espace Artistes"
                    : "Choisissez l'artiste de la représentation");
            return;
        }
        if (scene == null || day == null || time == null || !durationField.isValid()) {
            showError("Choisissez la scène, le jour, l'heure et la durée de la représentation");
            return;
        }
        tryCatch(
                "Planification de la représentation impossible",
                () -> {
                    Representation representation = new Representation(
                            minutesSinceStart(day.atTime(time)),
                            durationField.getValue(),
                            scene,
                            artiste
                    );
                    festival.ajouterRepresentation(representation);
                    addEntry(representation);
                    calendarView.setDate(day);
                }
        );
    }

    /**
     * Retourne le nombre de minutes entre le début du festival et une date.
     *
     * @param dateTime la date
     * @return le nombre de minutes, négatif si la date précède le festival
     */
    private int minutesSinceStart(LocalDateTime dateTime) {
        return (int) Duration.between(festival.getDebut(), dateTime).toMinutes();
    }

    /**
     * Ajoute l'entrée d'une représentation dans le calendrier de sa scène et la synchronise avec le modèle.
     *
     * @param representation la représentation
     */
    private void addEntry(Representation representation) {
        Entry<Representation> entry = new Entry<>(representation.getArtiste().getNom());
        entry.setUserObject(representation);
        entry.setInterval(intervalOf(representation));
        entry.intervalProperty().addListener((observable, oldInterval, newInterval) -> onIntervalChanged(entry, oldInterval, newInterval));
        entry.calendarProperty().addListener((observable, oldCalendar, newCalendar) -> onCalendarChanged(entry, oldCalendar, newCalendar));
        syncing = true;
        calendars.get(representation.getScene()).addEntry(entry);
        syncing = false;
    }

    /**
     * Retourne l'intervalle d'une représentation dans le calendrier.
     *
     * @param representation la représentation
     * @return l'intervalle allant du début à la fin de la représentation
     */
    private Interval intervalOf(Representation representation) {
        LocalDateTime start = festival.getDebut().plusMinutes(representation.getDecalageDebut());
        return new Interval(start, start.plusMinutes(representation.getDuree()));
    }

    /**
     * Répercute le déplacement ou le redimensionnement d'une entrée sur sa représentation.
     *
     * @param entry       l'entrée modifiée
     * @param oldInterval l'intervalle précédent
     * @param newInterval le nouvel intervalle
     */
    private void onIntervalChanged(Entry<Representation> entry, Interval oldInterval, Interval newInterval) {
        if (syncing)
            return;
        Representation representation = entry.getUserObject();
        try {
            festival.modifierRepresentation(
                    representation,
                    minutesSinceStart(newInterval.getStartDateTime()),
                    (int) Duration.between(newInterval.getStartDateTime(), newInterval.getEndDateTime()).toMinutes(),
                    representation.getScene()
            );
        } catch (Exception e) {
            syncing = true;
            entry.setInterval(oldInterval);
            syncing = false;
            showError("Déplacement de la représentation impossible :\n" + e.getMessage());
        }
    }

    /**
     * Répercute le changement de calendrier d'une entrée : suppression (touche Suppr) ou changement de scène.
     *
     * @param entry       l'entrée modifiée
     * @param oldCalendar le calendrier précédent
     * @param newCalendar le nouveau calendrier, null si l'entrée a été supprimée
     */
    private void onCalendarChanged(Entry<Representation> entry, Calendar<?> oldCalendar, Calendar<?> newCalendar) {
        if (syncing)
            return;
        Representation representation = entry.getUserObject();
        try {
            if (newCalendar == null)
                festival.supprimerRepresentation(representation);
            else
                festival.modifierRepresentation(representation, representation.getDecalageDebut(), representation.getDuree(), newCalendar.getName());
        } catch (Exception e) {
            syncing = true;
            entry.setCalendar(oldCalendar);
            syncing = false;
            showError("Modification de la représentation impossible :\n" + e.getMessage());
        }
    }

    /**
     * Crée le menu contextuel d'une entrée.
     *
     * @param entry l'entrée cliquée
     * @return le menu permettant de supprimer la représentation
     */
    private ContextMenu entryContextMenu(Entry<?> entry) {
        MenuItem edit = new MenuItem("Modifier la représentation…");
        edit.setOnAction(e -> editRepresentation(entry));
        MenuItem delete = new MenuItem("Supprimer la représentation");
        delete.setOnAction(e -> tryCatch(
                "Suppression de la représentation impossible",
                () -> {
                    festival.supprimerRepresentation((Representation) entry.getUserObject());
                    syncing = true;
                    entry.removeFromCalendar();
                    syncing = false;
                }
        ));
        return new ContextMenu(edit, delete);
    }

    /**
     * Modifie la scène, le jour, l'heure et la durée d'une représentation à partir d'un formulaire.
     * Permet notamment de la déplacer vers une autre semaine ou une autre scène, ce que le glisser-déposer ne permet pas.
     *
     * @param entry l'entrée de la représentation
     */
    @SuppressWarnings("unchecked")
    private void editRepresentation(Entry<?> entry) {
        Entry<Representation> representationEntry = (Entry<Representation>) entry;
        Representation representation = representationEntry.getUserObject();
        LocalDateTime start = festival.getDebut().plusMinutes(representation.getDecalageDebut());

        CustomValidField<ComboBox<String>> scene = new CustomValidField<>("Scène", new ComboBox<>()) {
            @Override
            public boolean isValid() {
                return node.getValue() != null;
            }
        };
        scene.node.getItems().setAll(festival.getScenes());
        scene.node.setValue(representation.getScene());
        CustomValidField<DatePicker> day = new CustomValidField<>("Jour", new DatePicker(start.toLocalDate())) {
            @Override
            public boolean isValid() {
                return node.getValue() != null;
            }
        };
        restrictToFestivalDays(day.node);
        CustomValidField<ComboBox<LocalTime>> time = new CustomValidField<>("Heure", new ComboBox<>()) {
            @Override
            public boolean isValid() {
                return node.getValue() != null;
            }
        };
        time.node.getItems().setAll(timeChoices());
        time.node.setValue(start.toLocalTime());
        CustomValidField<IntTextField> duration = new CustomValidField<>("Durée (min)", new IntTextField(true, true));
        duration.node.setValue(representation.getDuree());

        askValidForm(
                "Modifier la représentation de " + representation.getArtiste().getNom(),
                "Modification de la représentation impossible",
                new CustomValidField<?>[]{scene, day, time, duration},
                () -> {
                    festival.modifierRepresentation(
                            representation,
                            minutesSinceStart(day.node.getValue().atTime(time.node.getValue())),
                            duration.node.getValue(),
                            scene.node.getValue()
                    );
                    syncing = true;
                    representationEntry.setInterval(intervalOf(representation));
                    representationEntry.setCalendar(calendars.get(representation.getScene()));
                    syncing = false;
                    calendarView.setDate(day.node.getValue());
                }
        );
    }
}
