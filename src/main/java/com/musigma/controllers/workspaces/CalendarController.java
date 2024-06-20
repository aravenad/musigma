package com.musigma.controllers.workspaces;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Entry;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import com.musigma.models.exception.ArtisteException;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour l'espace de travail Calendrier.
 */
public class CalendarController extends WorkspaceController {
    public static WorkspaceController.WorkspaceRegister REGISTER = new WorkspaceController.WorkspaceRegister(
            "Planning",
            "/com/musigma/images/icons/calendar.png",
            "/com/musigma/views/calendar-view.fxml"
    );

    @FXML
    CalendarView calendarView; // (1)

    private ArrayList<Entry<?>> entries = new ArrayList<>();

    public void initialize(Festival festival) {
        super.initialize(festival);
        calendarView.setEntryFactory(createEntryParameter -> {
            Entry<?> entry = new Entry<>();
            entry.changeStartDate(LocalDate.now());
            entry.changeEndDate(LocalDate.now().plusDays(1));
            return entry;
        });
        Calendar calendar = new Calendar("Planning");
        calendar.addEntry(new Entry<>());
        CalendarSource calendarSource = new CalendarSource("Festival");
        calendarSource.getCalendars().add(calendar);
        calendarView.getCalendarSources().add(calendarSource);
        EventHandler<CalendarEvent> handler = this::addToModel;
        calendar.addEventHandler(handler);
    }

    private void addToModel(CalendarEvent calendarEvent) {
        if(calendarEvent.getEventType() == CalendarEvent.ANY){
            System.out.println("mofiqh");
        }
    }

    public void addRep(String title, LocalDateTime start, LocalDateTime end) throws ArtisteException {
        Entry<Representation> entry = new Entry<>(title);
        entry.setInterval(start, end);
        Representation representation = new Representation(
                ((start.getHour() * 60) + start.getMinute()) - ((festival.getStart().getHour() * 60) + festival.getStart().getMinute()),
                ((end.getHour() * 60) + end.getMinute()) - ((start.getHour() * 60) + start.getMinute()),
                "ogr",
                new Artiste("Rodrigo", "Rock", 10)
        );
        entry.setUserObject(representation);
        entries.add(entry);
    }

    public List<Entry<?>> getEntries() {
        return entries;
    }
}
