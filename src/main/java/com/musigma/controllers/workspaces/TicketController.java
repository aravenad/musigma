package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Avantage;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.TypeTicket;
import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.TypeTicketException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

/**
 * Contrôleur pour l'espace de travail Ticket.
 */
public class TicketController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Ticket",
            "/com/musigma/images/icons/ticket.png",
            "/com/musigma/views/ticket-view.fxml"
    );

    @FXML
    TextField textFieldType, textFieldQuantite, textFieldPrix, textFieldAvantage;

    @FXML
    Button buttonTicket, buttonAvantage;

    @FXML
    TabPane tabPane;

    @FXML
    ComboBox<Stock> comboAvantage;

    /**
     * Initialisation de l'espace de travail, restauration des onglets et ajout des écouteurs , definition des actions des boutons
     * definition de la comboBox et verification des tickets
     * @param festival Festival
     * @throws FestivalException si le festival est invalide
     * @throws AvantageException si l'avantage est invalide
     * @see #restoreTab()
     * @see #addListener()
     * @see #initializeComboBox()
     * @see #checkTicket()
     * @see #onAddTicketPressed()
     */
    @FXML
    public void initialize(Festival festival) {
        super.initialize(festival);
        restoreTab();
        addListener();
        initializeComboBox();
        checkTicket();
        buttonTicket.setOnAction(e -> {
            try {
                onAddTicketPressed();
            } catch (TypeTicketException | FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonAvantage.setOnAction(e -> onAddAvantagePressed());
    }
    /**
     * Ajoute un écouteur sur les champs de texte pour vérifier si les valeurs sont valides
     * @see #onAddTicketPressed()
     */
    private void addListener() {
        textFieldType.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals("Objet") || newValue.trim().isEmpty() || newValue.trim().isBlank() || newValue.matches(".*[^a-zA-Z-\\s].*")) {
                textFieldType.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldType.setStyle("-fx-border-color: transparent;");
            }
        });
        textFieldPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().isBlank()) {
                textFieldPrix.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldPrix.setStyle("-fx-border-color: transparent;");
            }
        });
        textFieldQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().isBlank()) {
                textFieldQuantite.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldQuantite.setStyle("-fx-border-color: transparent;");
            }
        });
    }

    /**
     * Ajoute un ticket à la liste des tickets. Si les champs de saisie sont valides, un ticket est créé et ajouté à la liste.
     * Sauvegarde le ticket dans la liste des tickets du festival. Crée un onglet pour le ticket. Vérifie si la liste des tickets est vide.
     * Sinon, les champs de saisie invalides sont surlignés en rouge.
     * @throws FestivalException si le festival est invalide
     * @throws TypeTicketException si le ticket est invalide
     * @see #addListener()
     */
    private void onAddTicketPressed() throws FestivalException, TypeTicketException {
        textFieldType.setStyle("-fx-border-color: transparent;");
        textFieldPrix.setStyle("-fx-border-color: transparent;");
        textFieldQuantite.setStyle("-fx-border-color: transparent;");
        if (textFieldType.getText().trim().equals("Objet") || textFieldType.getText().trim().isEmpty() || textFieldType.getText().trim().isBlank() || textFieldType.getText().matches(".*[^a-zA-Z-\\s].*")) {
            textFieldType.requestFocus();
            textFieldType.setStyle("-fx-border-color: crimson;");
        } else if (textFieldPrix.getText().trim().isEmpty() || textFieldPrix.getText().trim().isBlank()) {
            textFieldPrix.requestFocus();
            textFieldPrix.setStyle("-fx-border-color: crimson;");
        } else if (textFieldQuantite.getText().trim().isEmpty() || textFieldQuantite.getText().trim().isBlank()) {
            textFieldQuantite.requestFocus();
            textFieldQuantite.setStyle("-fx-border-color: crimson;");
        } else {
            TypeTicket ticket = new TypeTicket(textFieldType.getText(), Integer.parseInt(textFieldQuantite.getText()), Float.parseFloat(textFieldPrix.getText()));
            festival.addTicketType(ticket);
            createTab(ticket);
            textFieldType.setStyle("-fx-border-color: transparent;");
            textFieldPrix.setStyle("-fx-border-color: transparent;");
            textFieldQuantite.setStyle("-fx-border-color: transparent;");
            checkTicket();
        }
    }

    /**
     * Crée un onglet pour un ticket. Crée un TableView pour les avantages du ticket.
     * Ajoute les colonnes "Avantage" et "Quantité" au TableView.
     * Ajoute l'avantage au TableView.
     * Ajoute le TableView à l'onglet. Ajoute l'onglet au TabPane.
     * @param ticket TypeTicket Ticket a ajouter
     *
     */
    private void createTab(TypeTicket ticket) {
        Tab newTab = new Tab(ticket.getType());
        TableView<Avantage> newTableView = new TableView<>();
        TableColumn<Avantage, String> avantageColumn = new TableColumn<>("Avantage");
        TableColumn<Avantage, Integer> quantityColumn = new TableColumn<>("Quantité");
        newTableView.getColumns().add(avantageColumn);
        newTableView.getColumns().add(quantityColumn);
        newTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        avantageColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getStock().getName()));
        quantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantityByTicket()));
        newTab.setContent(newTableView);
        tabPane.getTabs().add(newTab);
    }

    /**
     * Restaure les onglets pour les tickets. Ajoute les avantages des tickets aux TableView.
     * @see #createTab(TypeTicket)
     */
    private void restoreTab() {
        for (int i = 0; i < festival.getTicketTypes().size(); i++) {
            TypeTicket ticketType = festival.getTicketTypes().get(i);
            createTab(ticketType);
            Tab tab = tabPane.getTabs().get(i);
            TableView<Avantage> tableView = (TableView<Avantage>) tab.getContent();
            for (Avantage avantage : ticketType.getAvantages()) {
                tableView.getItems().add(avantage);
            }
        }
    }

    /**
     * Initialise la ComboBox pour les avantages. Convertit les objets Stock en chaînes de caractères.
     * Ajoute les stocks du festival à la ComboBox.
     * @see #initialize(Festival)
     */
    private void initializeComboBox() {
        comboAvantage.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stock stock) {
                return stock != null ? stock.getName() : "";
            }
            @Override
            public Stock fromString(String string) {
                return null;
            }
        });
        comboAvantage.getItems().addAll(festival.getStocks());
    }

    /**
     * Vérifie si la liste des tickets est vide.
     * Si la liste est vide, la ComboBox, le champ de texte et le bouton pour les avantages sont masqués.
     * Sinon, la ComboBox, le champ de texte et le bouton pour les avantages sont affichés.
     * @see #initialize(Festival)
     */
    private void checkTicket() {
        if (festival.getTicketTypes().isEmpty()) {
            comboAvantage.setVisible(false);
            textFieldAvantage.setVisible(false);
            buttonAvantage.setVisible(false);
        } else {
            comboAvantage.setVisible(true);
            textFieldAvantage.setVisible(true);
            buttonAvantage.setVisible(true);
        }
    }

    /**
     * Ajoute un avantage à un ticket. Si les champs de saisie sont valides, un avantage est créé et ajouté à la liste.
     * Sauvegarde l'avantage dans la liste des avantages du ticket. Ajoute l'avantage au TableView.
     * @see #initialize(Festival)
     * @see #addListener()
     */
    private void onAddAvantagePressed() {
        if (comboAvantage.getSelectionModel().getSelectedItem() != null && !textFieldAvantage.getText().trim().isEmpty() && !textFieldAvantage.getText().trim().isBlank()) {
            try {
                TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());
                Stock stock = comboAvantage.getSelectionModel().getSelectedItem();
                int quantity = Integer.parseInt(textFieldAvantage.getText());
                Avantage avantage = new Avantage(ticket, stock, quantity);
                TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                tableView.getItems().add(avantage);
                festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex()).addAvantage(avantage);
            } catch (AvantageException e) {
                e.printStackTrace();
            } catch (TypeTicketException e) {
                throw new RuntimeException(e);
            } catch (NumberFormatException e) {
                // Gérer les cas où la conversion de textFieldAvantage en entier échoue
                textFieldAvantage.requestFocus();
                textFieldAvantage.setStyle("-fx-border-color: crimson;");
                return;
            }
        } else {
            textFieldAvantage.requestFocus();
            textFieldAvantage.setStyle("-fx-border-color: crimson;");
        }
        // Réinitialiser la bordure du champ texte et effacer le texte
        textFieldAvantage.setStyle("-fx-border-color: transparent;");
        textFieldAvantage.clear();
    }

}