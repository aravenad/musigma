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
import com.musigma.models.exception.FestivalException;
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
    RequiredTextField textFieldTicketType;

    @FXML
    IntTextField textFieldTicketQuantite, textFieldAvantageQuantite;

    @FXML
    FloatTextField textFieldTicketPrix;

    @FXML
    Button buttonAddTicket, buttonAddAvantage;

    @FXML
    TabPane tabPane;

    @FXML
    ComboBox<Stock> comboAvantageList;

    @FXML
    Label labelAvantageList, labelAvantageQuantite;

    /**
     * Initialisation de l'espace de travail, restauration des onglets et ajout des écouteurs , definition des actions des boutons
     * definition de la comboBox et verification des tickets
     * @param festival Festival
     * @see #restoreTab()
     * @see #initializeComboBox()
     * @see #checkTicket()
     * @see #onAddTicketPressed()
     */
    @FXML
    public void initialize(Festival festival) {
        super.initialize(festival);
        restoreTab();
        initializeComboBox();
        checkTicket();

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                int index = tabPane.getTabs().indexOf(newTab);
                TypeTicket ticket = festival.getTicketTypes().get(index);
                boolean isFirstTab = (index == 0 && tabPane.getTabs().size() == 1);
                renameTab(newTab, ticket, isFirstTab);
            }
        });

        buttonAddTicket.setOnAction(e -> {
            try {
                onAddTicketPressed();
            } catch (TypeTicketException | FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonAddAvantage.setOnAction(e -> onAddAvantagePressed());
    }

    /**
     * Ajoute un ticket. Si les champs de saisie sont valides, un ticket est créé et ajouté à la liste.
     * Sauvegarde le ticket dans la liste des tickets du festival. Crée un onglet pour le ticket.
     * @throws FestivalException si le festival est invalide
     * @throws TypeTicketException si le ticket est invalide
     * @see #createTab(TypeTicket)
     * @see #checkTicket()
     * @see #initialize(Festival)
     */
    private void onAddTicketPressed() throws FestivalException, TypeTicketException {
        textFieldTicketType.setStyle("-fx-border-color: transparent;");
        textFieldTicketPrix.setStyle("-fx-border-color: transparent;");
        textFieldTicketQuantite.setStyle("-fx-border-color: transparent;");
        if (textFieldTicketType.getText().trim().equals("Objet") || textFieldTicketType.getText().trim().isEmpty() || textFieldTicketType.getText().trim().isBlank() || textFieldTicketType.getText().matches(".*[^a-zA-Z-\\s].*")) {
            textFieldTicketType.requestFocus();
            textFieldTicketType.setStyle("-fx-border-color: crimson;");
        } else if (textFieldTicketPrix.getText().trim().isEmpty() || textFieldTicketPrix.getText().trim().isBlank()) {
            textFieldTicketPrix.requestFocus();
            textFieldTicketPrix.setStyle("-fx-border-color: crimson;");
        } else if (textFieldTicketQuantite.getText().trim().isEmpty() || textFieldTicketQuantite.getText().trim().isBlank()) {
            textFieldTicketQuantite.requestFocus();
            textFieldTicketQuantite.setStyle("-fx-border-color: crimson;");
        } else {
            TypeTicket ticket = new TypeTicket(textFieldTicketType.getText(), Integer.parseInt(textFieldTicketQuantite.getText()), Float.parseFloat(textFieldTicketPrix.getText()));
            festival.addTicketType(ticket);
            createTab(ticket);
            textFieldTicketType.setStyle("-fx-border-color: transparent;");
            textFieldTicketPrix.setStyle("-fx-border-color: transparent;");
            textFieldTicketQuantite.setStyle("-fx-border-color: transparent;");
            checkTicket();
        }
    }

    /**
     * Crée un onglet pour un ticket. Ajoute un TableView pour les avantages du ticket.
     * Ajoute une colonne pour la liste des avantages et une colonne pour la quantité.
     * Ajoute un bouton de suppression pour chaque ligne de la table.
     * @param ticket TypeTicket Ticket
     * @see #renameTab(Tab, TypeTicket, boolean)
     * @see #addDeleteButtonToTable(TableView, TableColumn)
     * @see #initialize(Festival)
     */
    private void createTab(TypeTicket ticket) {
        Tab newTab = new Tab(ticket.getType());
        renameTab(newTab, ticket, tabPane.getTabs().isEmpty());
        TableView<Avantage> avantageTableView = new TableView<>();
        TableColumn<Avantage, String> avantageListColumn = new TableColumn<>("Avantage");
        TableColumn<Avantage, Integer> avantageQuantityColumn = new TableColumn<>("Quantité");
        avantageTableView.getColumns().add(avantageListColumn);
        avantageTableView.getColumns().add(avantageQuantityColumn);
        avantageTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        avantageListColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getStock().getName()));
        avantageQuantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantityByTicket()));
        avantageTableView.setEditable(true);
        TableColumn<Avantage, Void> actionColumn = new TableColumn<>("Action");
        addDeleteButtonToTable(avantageTableView, actionColumn);
        newTab.setContent(avantageTableView);
        tabPane.getTabs().add(newTab);
        avantageQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter())); // Permet l'édition des quantités
        avantageQuantityColumn.setOnEditCommit(event -> {
            Avantage avantage = event.getRowValue();
            tryCatch(
                    "Impossible de modifier la quantité du stock",
                    () -> avantage.setQuantityByTicket(event.getNewValue()));
        });
    }

    /**
     * Ajoute un bouton de suppression à la table des avantages.
     * Crée un bouton "Supprimer" pour chaque ligne de la table.
     * @param tableView TableView<Avantage> Table des avantages
     * @param actionColumn TableColumn<Avantage, Void> Colonne d'action
     * @see #initialize(Festival)
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
        comboAvantageList.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stock stock) {
                return stock != null ? stock.getName() : "";
            }
            @Override
            public Stock fromString(String string) {
                return null;
            }
        });
        comboAvantageList.getItems().addAll(festival.getStocks());
    }

    /**
     * Vérifie si la liste des tickets est vide.
     * Si la liste est vide, la ComboBox, le champ de texte et le bouton pour les avantages sont masqués.
     * Sinon, la ComboBox, le champ de texte et le bouton pour les avantages sont affichés.
     * @see #initialize(Festival)
     */
    private void checkTicket() {
        if (festival.getTicketTypes().isEmpty()) {
            comboAvantageList.setVisible(false);
            textFieldAvantageQuantite.setVisible(false);
            buttonAddAvantage.setVisible(false);
            labelAvantageList.setVisible(false);
            labelAvantageQuantite.setVisible(false);
        } else {
            comboAvantageList.setVisible(true);
            textFieldAvantageQuantite.setVisible(true);
            buttonAddAvantage.setVisible(true);
            labelAvantageList.setVisible(true);
            labelAvantageQuantite.setVisible(true);
        }
    }

    /**
     * Ajoute un avantage à un ticket. Si les champs de saisie sont valides, un avantage est créé et ajouté à la liste.
     * Sauvegarde l'avantage dans la liste des avantages du ticket. Ajoute l'avantage au TableView.
     * @see #initialize(Festival)
     */
    private void onAddAvantagePressed() {
        if (comboAvantageList.getSelectionModel().getSelectedItem() != null && !textFieldAvantageQuantite.getText().trim().isEmpty() && !textFieldAvantageQuantite.getText().trim().isBlank()) {
            tryCatch(
                    "Ajout de l'avantage impossible",
                    () -> {
                        TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());
                        Stock stock = comboAvantageList.getSelectionModel().getSelectedItem();
                        int quantity = Integer.parseInt(textFieldAvantageQuantite.getText());
                        Avantage avantage = new Avantage(ticket, stock, quantity);
                        avantage.add();
                        TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                        tableView.getItems().add(avantage);
                        festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex()).addAvantage(avantage);
                    }
            );
        } else {
            textFieldAvantageQuantite.requestFocus();
        }
        textFieldAvantageQuantite.clearError();
    }

    /**
     * Renomme un onglet. Si l'onglet est sélectionné, le nom de l'onglet est affiché avec le nombre de tickets et le prix.
     * @param tab Onglet Tab
     * @param ticket TypeTicket Ticket
     * @param isFirstTab boolean Vrai si c'est le premier onglet
     */
    private void renameTab(Tab tab, TypeTicket ticket, boolean isFirstTab) {
        if (isFirstTab) {
            tab.setText(ticket.getType() + " (" + ticket.getQuantity() + " tickets, " + ticket.getPrice() + "€)");
        } else {
            tab.setText(ticket.getType());
        }

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                tab.setText(ticket.getType() + " (" + ticket.getQuantity() + " tickets, " + ticket.getPrice() + "€)");
            } else if (!isFirstTab) {
                tab.setText(ticket.getType());
            }
        });
    }
}