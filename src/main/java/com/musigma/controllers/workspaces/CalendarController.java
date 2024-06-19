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
        addEntry("Test", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    }

    private void handleCalendarEvent(CalendarEvent event) throws ArtisteException, FestivalException {
        if (event.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && event.getEntry().getCalendar() == calendar) {
            entries.add(event.getEntry());
            festival.addRepresentation(
                    new Representation(
                            ((event.getEntry().getStartAsLocalDateTime().getHour() * 60) + event.getEntry().getStartAsLocalDateTime().getMinute()) - ((festival.getStart().getHour() * 60) + festival.getStart().getMinute()),
                            10,
                            "ogr",
                            new Artiste("Rodrigo","Rock", 10)));
        }
    }

    public void addEntry(String title, LocalDateTime start, LocalDateTime end) throws ArtisteException {
        Entry<Representation> entry = new Entry<>(title);
        entry.setInterval(start, end);
        Representation Representation = new Representation(10, 10, "ogr", new Artiste("Rodrigo","Rock", 10));
        entry.setUserObject(Representation);
        entries.add(entry);
    }

    public List<Entry<?>> getEntries() {
        return entries;
    }
}
