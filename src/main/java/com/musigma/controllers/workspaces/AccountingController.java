package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.AbstractMap;
import java.util.Map;

import static com.musigma.controllers.Dialogs.tryCatch;


/**
 * Contrôleur pour l'espace de travail Ticket.
 */
public class AccountingController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Prévision",
            "/com/musigma/images/icons/accounting.png",
            "/com/musigma/views/accounting-view.fxml"
    );

    @FXML
    private TableView<Map.Entry<String, String>> tableview;

    @FXML
    private TableColumn<Map.Entry<String, String>, String> previsionColumn;

    @FXML
    private TableColumn<Map.Entry<String, String>, String> montantColumn;


    /**
     * Initialisation de l'espace de travail.
     * Ajoute les valeurs de prévision calculées à la table.
     *
     * @param festival le festival
     */
    @FXML
    public void initialize(Festival festival) {
        super.initialize(festival);
        tryCatch(
                "Erreur lors de l'optimisation de la prévision.",
                () -> {
                    previsionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
                    montantColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

                    ObservableList<Map.Entry<String, String>> data = FXCollections.observableArrayList();
                    data.add(new AbstractMap.SimpleEntry<>("Nombre de personnes recommandées :", String.valueOf(getRecommandedPerson())));
                    data.add(new AbstractMap.SimpleEntry<>("Nombre d'agents de sécurité recommandés :", String.valueOf(getRecommandedSecurity())));
                    tableview.setItems(data);

                });
    }

    /**
     * Calcule le nombre de personnes recommandées.
     *
     * @return le nombre de personnes recommandées
     */

    public int getRecommandedPerson() {
        return (int) (festival.getArea() / 0.42);
    }

    /**
     * Calcule le nombre d'agents de sécurité recommandés.
     *
     * @return le nombre d'agents de sécurité recommandés
     */
    public int getRecommandedSecurity() {
        return Math.max(3, (int) (festival.getArea() / 100));
    }
}