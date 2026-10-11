package com.musigma.controllers.workspaces;

import atlantafx.base.util.DoubleStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.CustomValidField;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Artiste;
import com.musigma.models.Festival;
import com.musigma.models.Representation;
import com.musigma.utils.exceptionMethods.Runner;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.musigma.controllers.Dialogs.*;

/**
 * Contrôleur pour l'espace de travail Artistes (UC4.1) : engagement des artistes et suivi de leurs représentations.
 */
public class ArtistController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     *
     * @see com.musigma.controllers.WorkspaceController
     */
    public static final WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Artistes",
            "/com/musigma/images/icons/artist.png",
            "/com/musigma/views/artist-view.fxml",
            "Engagez les artistes du festival et fixez leur cachet. Double-cliquez sur une valeur pour la modifier, sélectionnez un artiste pour voir ses représentations."
    );

    /**
     * En dessous de ce temps libre dans le planning (en minutes), l'engagement d'un artiste demande confirmation (UC4.1, emploi du temps plein).
     */
    static final long FULL_SCHEDULE_THRESHOLD = 60;

    /**
     * Format des dates de représentation, ex : « mer. 21/10 à 18:00 ».
     */
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE dd/MM 'à' HH:mm", Locale.FRENCH);

    @FXML
    TableView<Artiste> artistsTable;

    @FXML
    TableColumn<Artiste, String> nameColumn, genreColumn;

    @FXML
    TableColumn<Artiste, Double> feeColumn;

    @FXML
    TableColumn<Artiste, Integer> countColumn;

    @FXML
    TableColumn<Artiste, Void> actionColumn;

    @FXML
    TableView<Representation> representationsTable;

    @FXML
    TableColumn<Representation, String> sceneColumn, startColumn, durationColumn;

    @FXML
    Label totalFees, freeTime, representationsTitle;

    /**
     * Initialise le contrôleur : liste des artistes modifiable et représentations de l'artiste sélectionné.
     *
     * @param festival le festival
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        artistsTable.getItems().setAll(festival.getArtistes());
        artistsTable.setEditable(true);

        nameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getName()));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> commit(
                "Impossible de renommer l'artiste",
                () -> event.getRowValue().setName(event.getNewValue())
        ));

        genreColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getGenre()));
        genreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genreColumn.setOnEditCommit(event -> commit(
                "Impossible de modifier le genre de l'artiste",
                () -> event.getRowValue().setGenre(event.getNewValue())
        ));

        feeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>((double) f.getValue().getPrice()));
        feeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        feeColumn.setOnEditCommit(event -> commit(
                "Impossible de modifier le cachet de l'artiste",
                () -> event.getRowValue().setPrice(event.getNewValue().floatValue())
        ));

        countColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getRepresentations().size()));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Retirer");

            {
                deleteButton.setOnAction(e -> removeArtiste(getTableView().getItems().get(getIndex())));
            }

            @Override
            public void updateItem(Void item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setGraphic(isEmpty ? null : deleteButton);
            }
        });

        sceneColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getScene()));
        startColumn.setCellValueFactory(f -> new SimpleStringProperty(
                festival.getStart().plusMinutes(f.getValue().getStartDelta()).format(DATE_FORMAT)
        ));
        durationColumn.setCellValueFactory(f -> new SimpleStringProperty(formatDuration(f.getValue().getDuration())));
        artistsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldArtiste, artiste) -> showRepresentations(artiste));

        refreshSummary();
    }

    /**
     * Formate une durée en heures et minutes, ex : « 1 h 30 ».
     *
     * @param minutes la durée en minutes
     * @return la durée formatée
     */
    static String formatDuration(long minutes) {
        if (minutes < 60)
            return minutes + " min";
        return minutes % 60 == 0
                ? String.format("%d h", minutes / 60)
                : String.format("%d h %02d", minutes / 60, minutes % 60);
    }

    /**
     * Affiche les représentations d'un artiste.
     *
     * @param artiste l'artiste sélectionné, ou null
     */
    private void showRepresentations(Artiste artiste) {
        if (artiste == null) {
            representationsTitle.setText("Représentations");
            representationsTable.getItems().clear();
            return;
        }
        representationsTitle.setText("Représentations de " + artiste.getName());
        representationsTable.getItems().setAll(
                festival.getRepresentations().stream().filter(r -> r.getArtiste() == artiste).toList()
        );
    }

    /**
     * Applique une modification de la table au modèle, puis réaffiche la table :
     * une valeur refusée retrouve ainsi sa valeur précédente.
     *
     * @param errorMsg     le message d'erreur si le modèle refuse la modification
     * @param modification la modification du modèle
     */
    private void commit(String errorMsg, Runner modification) {
        tryCatch(errorMsg, modification);
        artistsTable.refresh();
        showRepresentations(artistsTable.getSelectionModel().getSelectedItem());
        refreshSummary();
    }

    /**
     * Engage un artiste à partir d'un formulaire (UC4.1).
     * Si le planning est presque plein, l'utilisateur doit confirmer.
     */
    @FXML
    private void addArtiste() {
        if (festival.getFreeMinutes() < FULL_SCHEDULE_THRESHOLD && !confirm(
                "Planning presque plein",
                String.format("Il ne reste que %s de libre sur l'ensemble des scènes. Engager quand même un artiste ?",
                        formatDuration(Math.max(0, festival.getFreeMinutes())))
        ))
            return;

        CustomValidField<RequiredTextField> name = new CustomValidField<>("Nom", new RequiredTextField());
        CustomValidField<RequiredTextField> genre = new CustomValidField<>("Genre musical", new RequiredTextField());
        CustomValidField<FloatTextField> fee = new CustomValidField<>("Cachet (€)", new FloatTextField(true, false));

        askValidForm(
                "Engager un artiste",
                "Engagement de l'artiste impossible",
                new CustomValidField<?>[]{name, genre, fee},
                () -> {
                    Artiste artiste = new Artiste(name.node.getText(), genre.node.getText(), fee.node.getValue());
                    festival.addArtiste(artiste);
                    artistsTable.getItems().add(artiste);
                    artistsTable.getSelectionModel().select(artiste);
                    refreshSummary();
                }
        );
    }

    /**
     * Retire un artiste du festival, après confirmation s'il a des représentations.
     *
     * @param artiste l'artiste à retirer
     */
    private void removeArtiste(Artiste artiste) {
        int count = artiste.getRepresentations().size();
        if (count > 0 && !confirm(
                "Retirer l'artiste",
                String.format("%s a %d représentation(s) planifiée(s), qui seront supprimées. Continuer ?", artiste.getName(), count)
        ))
            return;
        tryCatch(
                "Impossible de retirer l'artiste",
                () -> {
                    festival.removeArtiste(artiste);
                    artistsTable.getItems().remove(artiste);
                    refreshSummary();
                }
        );
    }

    /**
     * Affiche le total des cachets et le temps encore libre dans le planning.
     */
    private void refreshSummary() {
        totalFees.setText(String.format("Total des cachets : %.2f €", festival.getTotalFees()));
        freeTime.setText(String.format("Temps libre sur les scènes : %s", formatDuration(Math.max(0, festival.getFreeMinutes()))));
    }
}
