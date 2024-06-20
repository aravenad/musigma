package com.musigma.controllers.workspaces;

import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.IntTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Avantage;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.TypeTicket;
import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.StockException;
import com.musigma.models.exception.TypeTicketException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import static com.musigma.utils.Dialogs.tryCatch;

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
    RequiredTextField textFieldType;

    @FXML
    IntTextField textFieldQuantite;

    @FXML
    FloatTextField textFieldPrix, textFieldAvantage;

    @FXML
    Button buttonTicket, buttonAvantage;

    @FXML
    TabPane tabPane;

    @FXML
    ComboBox<Stock> comboAvantage;

    @FXML
    Label labelAvantage, labelQuantiteAvantage;

    /**
     * Initialisation de l'espace de travail, restauration des onglets et ajout des écouteurs , definition des actions des boutons
     * definition de la comboBox et verification des tickets
     * @param festival Festival
     * @throws FestivalException si le festival est invalide
     * @throws AvantageException si l'avantage est invalide
     * @see #restoreTab()
     * @see #initializeComboBox()
     * @see #checkTicket()
     * @see #onAddTicketPressed()
     */
    @FXML
    public void initialize(Festival festival) throws TypeTicketException {
        super.initialize(festival);
        restoreTab();
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
     * Ajoute un ticket à la liste des tickets. Si les champs de saisie sont valides, un ticket est créé et ajouté à la liste.
     * Sauvegarde le ticket dans la liste des tickets du festival. Crée un onglet pour le ticket. Vérifie si la liste des tickets est vide.
     * Sinon, les champs de saisie invalides sont surlignés en rouge.
     * @throws FestivalException si le festival est invalide
     * @throws TypeTicketException si le ticket est invalide
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
     * Ajoute la possibilité d'éditer les quantités.
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
        newTableView.setEditable(true);
        TableColumn<Avantage, Void> actionColumn = new TableColumn<>("Action");
        addDeleteButtonToTable(newTableView, actionColumn);
        newTab.setContent(newTableView);
        tabPane.getTabs().add(newTab);
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter())); // Permet l'édition des quantités
        quantityColumn.setOnEditCommit(event -> {
            Avantage avantage = event.getRowValue();
            tryCatch(
                    "Impossible de modifier la quantité du stock",
                    () -> avantage.setQuantityByTicket(event.getNewValue()));
        });
    }

    /**
     * Ajoute un bouton de suppression à la table des avantages.
     * Crée un bouton "Supprimer" pour chaque ligne de la table.
     */
    private void addDeleteButtonToTable(TableView<Avantage> tableView, TableColumn<Avantage, Void> actionColumn) {
        Callback<TableColumn<Avantage, Void>, TableCell<Avantage, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Avantage, Void> call(final TableColumn<Avantage, Void> param) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(Void item, boolean isEmpty) {
                        super.updateItem(item, isEmpty);
                        Button btn = new Button("Supprimer");
                        if (isEmpty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                            btn.setOnAction((e) -> {
                                Avantage avantage = getTableView().getItems().get(getIndex());
                                tryCatch(
                                        "Impossible de supprimer l'avantage",
                                        () -> {
                                            festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex()).removeAvantage(avantage);
                                            getTableView().getItems().remove(avantage);
                                        });
                            });
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
        tableView.getColumns().add(actionColumn);
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
            labelAvantage.setVisible(false);
            labelQuantiteAvantage.setVisible(false);
        } else {
            comboAvantage.setVisible(true);
            textFieldAvantage.setVisible(true);
            buttonAvantage.setVisible(true);
            labelAvantage.setVisible(true);
            labelQuantiteAvantage.setVisible(true);
        }
    }

    /**
     * Ajoute un avantage à un ticket. Si les champs de saisie sont valides, un avantage est créé et ajouté à la liste.
     * Sauvegarde l'avantage dans la liste des avantages du ticket. Ajoute l'avantage au TableView.
     * @see #initialize(Festival)
     */
    private void onAddAvantagePressed() {
        if (comboAvantage.getSelectionModel().getSelectedItem() != null && !textFieldAvantage.getText().trim().isEmpty() && !textFieldAvantage.getText().trim().isBlank()) {
            try {
                TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());
                Stock stock = comboAvantage.getSelectionModel().getSelectedItem();
                int quantity = Integer.parseInt(textFieldAvantage.getText());
                Avantage avantage = new Avantage(ticket, stock, quantity);
                avantage.add();
                TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                tableView.getItems().add(avantage);
                festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex()).addAvantage(avantage);
            } catch (AvantageException e) {
                e.printStackTrace();
            } catch (TypeTicketException | StockException e) {
                throw new RuntimeException(e);
            } catch (NumberFormatException e) {
                textFieldAvantage.requestFocus();
                return;
            }
        } else {
            textFieldAvantage.requestFocus();
        }
        textFieldAvantage.clearError();
    }
}