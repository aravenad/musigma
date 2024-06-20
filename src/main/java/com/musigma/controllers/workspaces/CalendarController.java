package com.musigma.controllers.workspaces;

import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.IntTextField;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.TypeTicketException;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
    CalendarView calendarView;

    @FXML
    Button addEventButton;

    @FXML
    IntTextField dateDebut;

    @FXML
    IntTextField dateFin;

    @FXML
    TextField nomEvent;

    @FXML
    TextField scene;

    private ArrayList<Entry<?>> entries = new ArrayList<>();

    public void initialize(Festival festival) {
        super.initialize(festival);
        Calendar calendar = new Calendar("Planning");
        CalendarSource calendarSource = new CalendarSource("Festival");
        calendarSource.getCalendars().add(calendar);
        calendarView.getCalendarSources().add(calendarSource);
        EventHandler<CalendarEvent> handler = this::addToModel;
        calendar.addEventHandler(handler);

        //ajoutes des Entrée dans la liste entries pour ensuite les ajouter dans le planning
        for(Representation r : festival.getRepresentations()){
            Entry<?> tempEntry = new Entry<>(r.getArtiste().getName());
            tempEntry.changeStartTime(LocalTime.from(festival.getStart().plusMinutes(r.getStartDelta())));
            tempEntry.changeEndTime(LocalTime.from(festival.getStart().plusMinutes(r.getStartDelta())).plusMinutes(r.getDuration()));
            entries.add(tempEntry);
        }

        //ajout des entrée sur le planning
        for(Entry<?> e : entries){
            calendar.addEntry(e);
        }


        //Ajoute de l'event avec les parametre rentré
        addEventButton.setOnAction(e -> {
            Entry<Representation> entry = new Entry<>(nomEvent.getText());
            entry.changeStartTime(LocalTime.of(dateDebut.getValue(),0));
            entry.changeEndTime(LocalTime.of(dateFin.getValue(),0));
            calendar.addEntry(entry);
            try {
                addRep(nomEvent.getText(),
                        LocalDateTime.of(2024,06,20, dateDebut.getValue(),0),
                        entry.getEndTime().getHour() - entry.getStartTime().getHour(),
                        scene.getText()
                );
            } catch (ArtisteException ex) {
                throw new RuntimeException(ex);
            } catch (FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    //Est censé etre la methode appelé lors ce que le calendrier est modifié
    private void addToModel(CalendarEvent calendarEvent) {
        if(calendarEvent.getEventType() == CalendarEvent.ANY){
            System.out.println("mofiqh");
        }
    }

    //Ajoute au modele
    public void addRep(String artisteName, LocalDateTime start, int duration, String scene) throws ArtisteException, FestivalException {
        Entry<Representation> entry = new Entry<>(artisteName);
        entry.setInterval(start, start.plusHours(duration));
        Representation representation = new Representation(
                ((start.getHour() * 60) + start.getMinute()) - ((festival.getStart().getHour() * 60) + festival.getStart().getMinute()),
                ((start.plusHours(duration).getHour() * 60) + start.plusHours(duration).getMinute()) - ((start.getHour() * 60) + start.getMinute()),
                scene,
                new Artiste(artisteName, "Rock", 10)
        );
        entry.setUserObject(representation);
        festival.addRepresentation(representation);
    }
}
