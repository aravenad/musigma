package com.musigma.controllers.workspaces;

import com.calendarfx.model.*;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.WeekView;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.IntTextField;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.TypeTicketException;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    DetailedWeekView detailWeekView;

    @FXML
    Button addEventButton;

    @FXML
    IntTextField dateDebut;

    @FXML
    IntTextField dateFin;

    @FXML
    TextField nomEvent;

    private ArrayList<Entry<?>> entries = new ArrayList<>();

    public void initialize(Festival festival) throws TypeTicketException {
        detailWeekView.setAdjustToFirstDayOfWeek(false);
        detailWeekView.setNumberOfDays(10);
        super.initialize(festival);
        Calendar calendar = new Calendar("Planning");
        calendar.addEntry(new Entry<>());
        CalendarSource calendarSource = new CalendarSource("Festival");
        calendarSource.getCalendars().add(calendar);
        detailWeekView.getCalendarSources().add(calendarSource);
        EventHandler<CalendarEvent> handler = this::addToModel;
        calendar.addEventHandler(handler);

        addEventButton.setOnAction(e -> {
            Entry<Event> entry = new Entry<>(nomEvent.getText());
            entry.changeStartTime(LocalTime.of(dateDebut.getValue(),0));
            entry.changeEndTime(LocalTime.of(dateDebut.getValue(),0));
            calendar.addEntry(entry);
        });
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
