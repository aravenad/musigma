package com.musigma.controllers.workspaces;

import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.CustomValidField;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import java.util.ArrayList;

import static com.musigma.utils.Dialogs.*;

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
    TabPane tabPane;

    /**
     * Initialisation de l'espace de travail, restauration des onglets et ajout des écouteurs , definition des actions des boutons
     * definition de la comboBox et verification des tickets
     * @param festival Festival
     * @see #restoreTab()
     */
    @FXML
    public void initialize(Festival festival) {
        super.initialize(festival);
        restoreTab();
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                int index = tabPane.getTabs().indexOf(newTab);
                TypeTicket ticket = festival.getTicketTypes().get(index);
                boolean isFirstTab = (index == 0 && tabPane.getTabs().size() == 1);
                renameTab(newTab, ticket, isFirstTab);
            }
        });
    }

    /**
     * Ajoute un ticket. Si les champs de saisie sont valides, un ticket est créé et ajouté à la liste.
     * Sauvegarde le ticket dans la liste des tickets du festival. Crée un onglet pour le ticket.
     * @see #createTab(TypeTicket)
     * @see #initialize(Festival)
     */
    @FXML
    private void addTicket() {
        CustomValidField<RequiredTextField> nameTicket = new CustomValidField<>("Nom", new RequiredTextField());
        CustomValidField<IntTextField> quantiteTicket = new CustomValidField<>("Quantité", new IntTextField(true, true));
        CustomValidField<FloatTextField> priceTicket = new CustomValidField<>("Prix", new FloatTextField(true, false));

        askValidForm(
                "Ajouter un type de ticket",
                "Ajout d'un type de ticket impossible",
                new CustomValidField[]{nameTicket, quantiteTicket, priceTicket},
                () -> {
                    TypeTicket ticket = new TypeTicket(nameTicket.node.getText(), quantiteTicket.node.getValue(), priceTicket.node.getValue());
                    festival.addTicketType(ticket);
                    createTab(ticket);
                }
        );
    }

    @FXML
    private void updTicket() {
        if (festival.getTicketTypes().isEmpty())
            newError("Aucun ticket disponible");
        else {
            TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());

            CustomValidField<RequiredTextField> nameTicket = new CustomValidField<>("Nom", new RequiredTextField());
            nameTicket.node.setText(ticket.getType());
            CustomValidField<IntTextField> quantiteTicket = new CustomValidField<>("Quantité", new IntTextField(true, true));
            quantiteTicket.node.setValue(ticket.getQuantity());
            CustomValidField<FloatTextField> priceTicket = new CustomValidField<>("Prix", new FloatTextField(true, false));
            priceTicket.node.setValue(ticket.getPrice());

            askValidForm(
            "Modifier le ticket " + ticket.getType(),
        "Modification d'un type de ticket impossible",
                new CustomValidField[]{nameTicket, quantiteTicket, priceTicket},
                () -> {
                    ticket.setType(nameTicket.node.getText());
                    ticket.setQuantity(quantiteTicket.node.getValue());
                    ticket.setPrice(priceTicket.node.getValue());
                    tabPane.getTabs().clear();
                    restoreTab();
                }
            );
        }
    }

    @FXML
    private void removeTicket() {
        if (festival.getTicketTypes().isEmpty())
            newError("Aucun ticket disponible");
        else {
            TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());
            tryCatch(
        "Impossible de supprimer le type de ticket",
                () -> {
                    festival.removeTicketType(ticket);
                    tabPane.getTabs().clear();
                    restoreTab();
                }
            );
        }
    }

    /**
     * Ajoute un avantage à un ticket. Si les champs de saisie sont valides, un avantage est créé et ajouté à la liste.
     * Sauvegarde l'avantage dans la liste des avantages du ticket. Ajoute l'avantage au TableView.
     * @see #initialize(Festival)
     */
    @FXML
    private void addAvantage() {
        if (festival.getTicketTypes().isEmpty())
            newError("Aucun ticket disponible");
        else {
            TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());

            CustomValidField<ComboBox<Stock>> stockAvantage = new CustomValidField<>("Stock", new ComboBox<>()) {
                public boolean isValid() {
                    return node.getSelectionModel().getSelectedItem() != null;
                }
            };
            stockAvantage.node.getItems().addAll(festival.getStocks());
            CustomValidField<IntTextField> quantityByTicket = new CustomValidField<>("Quantité par ticket", new IntTextField(true, false));

            askValidForm(
                "Ajouter un avantage",
            "Ajout d'un avantage impossible",
                    new CustomValidField[]{stockAvantage, quantityByTicket},
                    () -> {
                        Avantage avantage = new Avantage(
                                ticket,
                                stockAvantage.node.getSelectionModel().getSelectedItem(),
                                quantityByTicket.node.getValue()
                        );
                        avantage.add();
                        TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                        tableView.getItems().add(avantage);
                        ticket.addAvantage(avantage);
                    }
            );
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
        avantageQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter())); // Permet l'édition des quantités
        avantageQuantityColumn.setOnEditCommit(event -> {
            Avantage avantage = event.getRowValue();
            tryCatch(
        "Impossible de modifier la quantité du stock",
                () -> avantage.setQuantityByTicket(event.getNewValue()));
        });

        tabPane.getTabs().add(newTab);
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
     * Renomme un onglet. Si l'onglet est sélectionné, le nom de l'onglet est affiché avec le nombre de tickets et le prix.
     * @param tab Onglet Tab
     * @param ticket TypeTicket Ticket
     * @param isFirstTab boolean Vrai si c'est le premier onglet
     */
    private void renameTab(Tab tab, TypeTicket ticket, boolean isFirstTab) {
        if (isFirstTab) {
            tab.setText(ticket.getType() + " (" + ticket.getQuantity() + " tickets, " + ticket.getPrice() + " €)");
        } else {
            tab.setText(ticket.getType());
        }

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                tab.setText(ticket.getType() + " (" + ticket.getQuantity() + " tickets, " + ticket.getPrice() + " €)");
            } else if (!isFirstTab) {
                tab.setText(ticket.getType());
            }
        });
    }
}