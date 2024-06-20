package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.exception.TypeTicketException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.AbstractMap;
import java.util.Map;

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
     * Initialisation du contrôleur.
     * Ajoute les valeur de prévision calculées à la table.
     * @param festival le festival
     * @throws TypeTicketException si le type de billet est incorrect
     */
    @FXML
    public void initialize(Festival festival) throws TypeTicketException {
        super.initialize(festival);

        previsionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
        montantColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

        ObservableList<Map.Entry<String, String>> data = FXCollections.observableArrayList();
        data.add(new AbstractMap.SimpleEntry<>("Nombre de personnes recommandées :", String.valueOf(getRecommandedPerson())));
        data.add(new AbstractMap.SimpleEntry<>("Nombre d'agents de sécurité recommandés :", String.valueOf(getRecommandedSecurity())));
        tableview.setItems(data);
    }
    /**
     * Calcule le nombre de personnes recommandées.
     * @return le nombre de personnes recommandées
     */
    public int getRecommandedPerson() {
        return (int) (festival.getArea() / 0.42);
    }
    /**
     * Calcule le nombre d'agents de sécurité recommandés.
     * @return le nombre d'agents de sécurité recommandés
     */
    public int getRecommandedSecurity() {
        return Math.max(3, (int) (festival.getArea() / 100));
    }
}
