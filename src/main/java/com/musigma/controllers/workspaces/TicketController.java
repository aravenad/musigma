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
import com.musigma.utils.exceptionMethods.Runner;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static com.musigma.controllers.Dialogs.*;

/**
 * Contrôleur pour l'espace de travail Tickets (UC3.1) : types de tickets et leurs avantages.
 */
public class TicketController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static final WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Tickets",
            "/com/musigma/images/icons/ticket.png",
            "/com/musigma/views/ticket-view.fxml",
            "Créez les types de tickets avec leur prix et la surface occupée par personne, puis ajoutez-leur des avantages puisés dans les stocks."
    );

    /**
     * Format des montants, ex : « 100 000,00 € ».
     */
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    @FXML
    TableView<TypeTicket> ticketsTable;

    @FXML
    TableColumn<TypeTicket, String> typeColumn, quantityColumn, priceColumn, surfaceColumn, revenueColumn;

    @FXML
    TableView<Avantage> avantagesTable;

    @FXML
    TableColumn<Avantage, String> stockColumn, consumptionColumn;

    @FXML
    TableColumn<Avantage, Integer> quantityByTicketColumn;

    @FXML
    TableColumn<Avantage, Void> avantageActionColumn;

    @FXML
    Button editButton, removeButton, addAvantageButton;

    @FXML
    Label avantagesTitle, totalRevenue;

    /**
     * Initialise l'espace de travail : liste des types de tickets et avantages du type sélectionné.
     *
     * @param festival le festival
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        typeColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getType()));
        quantityColumn.setCellValueFactory(f -> new SimpleStringProperty(Integer.toString(f.getValue().getQuantite())));
        priceColumn.setCellValueFactory(f -> new SimpleStringProperty(formatNumber(f.getValue().getPrix())));
        surfaceColumn.setCellValueFactory(f -> new SimpleStringProperty(formatNumber(f.getValue().getSurface())));
        revenueColumn.setCellValueFactory(f -> new SimpleStringProperty(CURRENCY.format(f.getValue().getRecette())));
        ticketsTable.setRowFactory(table -> {
            TableRow<TypeTicket> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    updTicket();
            });
            return row;
        });

        stockColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getStock().getNom()));
        quantityByTicketColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantiteParTicket()));
        quantityByTicketColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityByTicketColumn.setOnEditCommit(event -> commit(
                "Impossible de modifier la quantité de l'avantage",
                () -> event.getRowValue().setQuantiteParTicket(event.getNewValue())
        ));
        consumptionColumn.setCellValueFactory(f -> new SimpleStringProperty(Long.toString(f.getValue().getQuantiteTotale())));
        avantageActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Retirer");

            {
                deleteButton.setOnAction(e -> {
                    Avantage avantage = getTableView().getItems().get(getIndex());
                    commit(
                            "Impossible de retirer l'avantage",
                            () -> avantage.getTypeTicket().retirerAvantage(avantage)
                    );
                });
            }

            @Override
            public void updateItem(Void item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setGraphic(isEmpty ? null : deleteButton);
            }
        });
        avantagesTable.setEditable(true);

        ticketsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldTicket, ticket) -> refreshAvantages());
        refresh();
        ticketsTable.getSelectionModel().selectFirst();
    }

    /**
     * Formate un nombre sans décimales inutiles.
     *
     * @param value le nombre
     * @return le nombre formaté
     */
    private static String formatNumber(float value) {
        return new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }

    /**
     * Retourne le type de ticket sélectionné.
     *
     * @return le type de ticket sélectionné, ou null
     */
    private TypeTicket selected() {
        return ticketsTable.getSelectionModel().getSelectedItem();
    }

    /**
     * Réaffiche les types de tickets en gardant la sélection, puis leurs avantages.
     */
    private void refresh() {
        TypeTicket selected = selected();
        ticketsTable.getItems().setAll(festival.getTypesTicket());
        if (selected != null && festival.getTypesTicket().contains(selected))
            ticketsTable.getSelectionModel().select(selected);
        ticketsTable.refresh();
        totalRevenue.setText(String.format("Recette des tickets : %s", CURRENCY.format(festival.getBilan().totalRecettes())));
        refreshAvantages();
    }

    /**
     * Affiche les avantages du type de ticket sélectionné et active les actions qui le concernent.
     */
    private void refreshAvantages() {
        TypeTicket ticket = selected();
        editButton.setDisable(ticket == null);
        removeButton.setDisable(ticket == null);
        addAvantageButton.setDisable(ticket == null);
        avantagesTitle.setText(ticket == null ? "Avantages" : "Avantages du ticket « " + ticket.getType() + " »");
        avantagesTable.getItems().setAll(ticket == null ? java.util.List.of() : ticket.getAvantages());
        avantagesTable.refresh();
    }

    /**
     * Applique une modification au modèle puis réaffiche l'écran : une valeur refusée retrouve sa valeur précédente.
     *
     * @param errorMsg     le message d'erreur si le modèle refuse la modification
     * @param modification la modification du modèle
     */
    private void commit(String errorMsg, Runner modification) {
        tryCatch(errorMsg, modification);
        refresh();
    }

    /**
     * Ajoute un type de ticket à partir d'un formulaire.
     */
    @FXML
    private void addTicket() {
        CustomValidField<RequiredTextField> nameTicket = new CustomValidField<>("Nom", new RequiredTextField());
        CustomValidField<IntTextField> quantiteTicket = new CustomValidField<>("Quantité", new IntTextField(true, false));
        CustomValidField<FloatTextField> priceTicket = new CustomValidField<>("Prix (€)", new FloatTextField(true, false));
        CustomValidField<FloatTextField> surfaceTicket = new CustomValidField<>("Surface par personne (m²)", new FloatTextField(true, true));
        surfaceTicket.node.setValue(TypeTicket.SURFACE_PAR_DEFAUT);

        askValidForm(
                "Ajouter un type de ticket",
                "Ajout d'un type de ticket impossible",
                new CustomValidField<?>[]{nameTicket, quantiteTicket, priceTicket, surfaceTicket},
                () -> {
                    TypeTicket ticket = new TypeTicket(nameTicket.node.getText(), quantiteTicket.node.getValue(), priceTicket.node.getValue());
                    ticket.setSurface(surfaceTicket.node.getValue());
                    festival.ajouterTypeTicket(ticket);
                    refresh();
                    ticketsTable.getSelectionModel().select(ticket);
                }
        );
    }

    /**
     * Modifie le type de ticket sélectionné à partir d'un formulaire. La modification est atomique.
     */
    @FXML
    private void updTicket() {
        TypeTicket ticket = selected();
        if (ticket == null)
            return;
        CustomValidField<RequiredTextField> nameTicket = new CustomValidField<>("Nom", new RequiredTextField());
        nameTicket.node.setText(ticket.getType());
        CustomValidField<IntTextField> quantiteTicket = new CustomValidField<>("Quantité", new IntTextField(true, false));
        quantiteTicket.node.setValue(ticket.getQuantite());
        CustomValidField<FloatTextField> priceTicket = new CustomValidField<>("Prix (€)", new FloatTextField(true, false));
        priceTicket.node.setValue(ticket.getPrix());
        CustomValidField<FloatTextField> surfaceTicket = new CustomValidField<>("Surface par personne (m²)", new FloatTextField(true, true));
        surfaceTicket.node.setValue(ticket.getSurface());

        askValidForm(
                "Modifier le ticket " + ticket.getType(),
                "Modification du type de ticket impossible",
                new CustomValidField<?>[]{nameTicket, quantiteTicket, priceTicket, surfaceTicket},
                () -> {
                    festival.modifierTypeTicket(
                            ticket,
                            nameTicket.node.getText(),
                            quantiteTicket.node.getValue(),
                            priceTicket.node.getValue(),
                            surfaceTicket.node.getValue()
                    );
                    refresh();
                }
        );
    }

    /**
     * Supprime le type de ticket sélectionné, après confirmation.
     */
    @FXML
    private void removeTicket() {
        TypeTicket ticket = selected();
        if (ticket == null || !confirm(
                "Supprimer le type de ticket",
                String.format("Supprimer le type de ticket « %s » et ses %d avantage(s) ?", ticket.getType(), ticket.getAvantages().size())
        ))
            return;
        commit(
                "Impossible de supprimer le type de ticket",
                () -> festival.supprimerTypeTicket(ticket)
        );
    }

    /**
     * Ajoute un avantage au type de ticket sélectionné à partir d'un formulaire.
     */
    @FXML
    private void addAvantage() {
        TypeTicket ticket = selected();
        if (ticket == null)
            return;
        if (festival.getStocks().isEmpty()) {
            showError("Aucun stock : ajoutez d'abord un stock dans l'espace Stocks");
            return;
        }
        CustomValidField<ComboBox<Stock>> stockAvantage = new CustomValidField<>("Stock", new ComboBox<>()) {
            @Override
            public boolean isValid() {
                return node.getSelectionModel().getSelectedItem() != null;
            }
        };
        stockAvantage.node.getItems().addAll(festival.getStocks());
        CustomValidField<IntTextField> quantityByTicket = new CustomValidField<>("Quantité par ticket", new IntTextField(true, true));
        quantityByTicket.node.setValue(1);

        askValidForm(
                "Ajouter un avantage au ticket " + ticket.getType(),
                "Ajout d'un avantage impossible",
                new CustomValidField<?>[]{stockAvantage, quantityByTicket},
                () -> {
                    new Avantage(
                            ticket,
                            stockAvantage.node.getSelectionModel().getSelectedItem(),
                            quantityByTicket.node.getValue()
                    ).connecter();
                    refresh();
                }
        );
    }
}
