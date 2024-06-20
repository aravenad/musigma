package com.musigma.controllers.workspaces;

import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.IntTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.RepresentationException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

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
    IntTextField dateDebut, dateFin, scene;

    @FXML
    RequiredTextField nomEvent;

    private ArrayList<Entry<?>> entries = new ArrayList<>();

    public void initialize(Festival festival) {
        super.initialize(festival);
        Calendar calendar = new Calendar("Planning");
        CalendarSource calendarSource = new CalendarSource("Festival");
        calendarSource.getCalendars().add(calendar);
        calendarView.getCalendarSources().add(calendarSource);
        calendar.addEventHandler(this::addToModel);

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
            } catch (ArtisteException | FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void addToModel(Event event){
        if(event.getEventType() == CalendarEvent.ENTRY_TITLE_CHANGED){
            //changer nom
            for(Representation r : festival.getRepresentations()){
                if(r.getArtiste().getName().equals(((Entry<?>) event.getSource()).getTitle())){
                    try {
                        r.getArtiste().setName(((Entry<?>) event.getSource()).getTitle());
                    } catch (ArtisteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if(event.getEventType() == CalendarEvent.ENTRY_INTERVAL_CHANGED){
            //changer l'heure de début et de fin
            for(Representation r : festival.getRepresentations()){
                if(r.getArtiste().getName().equals(event.getSource())){
                    try {
                        r.setStartDelta(((Entry<?>) event.getSource()).getStartTime().getHour() * 60 + ((Entry<?>) event.getSource()).getStartTime().getMinute());
                    } catch (RepresentationException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        r.setDuration((((Entry<?>) event.getSource()).getEndTime().getHour() * 60 + ((Entry<?>) event.getSource()).getEndTime().getMinute()) - ((Entry<?>) event.getSource()).getStartTime().getHour() * 60 + ((Entry<?>) event.getSource()).getStartTime().getMinute());
                    } catch (RepresentationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if(event.getEventType().equals(CalendarEvent.NULL_SOURCE_TARGET)){
            //pour supprimer l'artiste
            for(Representation r : festival.getRepresentations()){
                if(r.getArtiste().getName().equals(((Entry<?>) event.getSource()).getTitle())){
                    try {
                        festival.removeRepresentation(r);
                    } catch (FestivalException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
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
