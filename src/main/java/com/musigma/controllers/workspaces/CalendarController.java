package com.musigma.controllers.workspaces;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Artiste;
import com.musigma.models.Representation;
import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarController extends WorkspaceController {
    public static WorkspaceController.WorkspaceRegister REGISTER = new WorkspaceController.WorkspaceRegister(
            "Planning",
            "/com/musigma/images/icons/calendar.png",
            "/com/musigma/views/calendar-view.fxml"
    );

    @FXML
    CalendarView calendarView;

    private ArrayList<Entry<?>> entries = new ArrayList<>();
    private Calendar calendar = new Calendar("My Calendar");

    @FXML
    public void initialize() throws ArtisteException {
        calendarView.getCalendarSources().clear();
        calendarView.getCalendarSources().add(new com.calendarfx.model.CalendarSource("My Calendars") {{
            getCalendars().add(calendar);
        }});

        calendar.addEventHandler(this::handleCalendarEvent);

        addRep("Test Entry", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    }

    private void handleCalendarEvent(Event event) {
        if (event instanceof CalendarEvent) {
            CalendarEvent calendarEvent = (CalendarEvent) event;
            if (calendarEvent.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && calendarEvent.getEntry().getCalendar() == calendar) {
                Entry<?> entry = calendarEvent.getEntry();
                try {
                    entries.add(entry);
                    festival.addRepresentation(
                            new Representation(
                                    ((entry.getStartAsLocalDateTime().getHour() * 60) + entry.getStartAsLocalDateTime().getMinute()) - ((festival.getStart().getHour() * 60) + festival.getStart().getMinute()),
                                    10,
                                    "ogr",
                                    new Artiste("Rodrigo", "Rock", 10))
                    );
                    System.out.println("New entry added: " + entry.getTitle());
                } catch (ArtisteException | FestivalException e) {
                    e.printStackTrace();
                }
            }
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
        calendar.addEntry(entry);
        entries.add(entry);
    }

    public List<Entry<?>> getEntries() {
        return entries;
    }
}
