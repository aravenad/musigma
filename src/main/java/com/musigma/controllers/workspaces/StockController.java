package com.musigma.controllers.workspaces;

import atlantafx.base.util.DoubleStringConverter;
import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.CustomValidField;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.IntTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.utils.exceptionMethods.Runner;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

import static com.musigma.controllers.Dialogs.*;

/**
 * Contrôleur pour l'espace de travail Stock (UC3.3) : stocks du festival, limites et besoins des tickets.
 */
public class StockController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     * Définit le nom, l'icône et la vue de l'espace de travail.
     *
     * @see com.musigma.controllers.WorkspaceController
     */
    public static final WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Stocks",
            "/com/musigma/images/icons/stock.png",
            "/com/musigma/views/stock-view.fxml",
            "Listez les ressources du festival. Cochez « Limité » pour une ressource que les tickets ne peuvent pas dépasser ; la colonne Consommation indique ce que demandent les tickets."
    );

    @FXML
    TableView<Stock> tableView;

    @FXML
    TableColumn<Stock, String> nameColumn, consumedColumn;

    @FXML
    TableColumn<Stock, Integer> quantityColumn;

    @FXML
    TableColumn<Stock, Boolean> fixedColumn;

    @FXML
    TableColumn<Stock, Double> priceColumn;

    @FXML
    TableColumn<Stock, Void> actionColumn;

    @FXML
    Label total, missing;

    /**
     * Initialise le contrôleur : charge les stocks du festival dans la table et rend leurs valeurs modifiables.
     *
     * @param festival le festival
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        tableView.getItems().setAll(festival.getStocks());
        tableView.setEditable(true);

        nameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getNom()));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> commit(
                "Impossible de renommer le stock",
                () -> festival.renommerStock(event.getRowValue(), event.getNewValue())
        ));

        quantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantite()));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setOnEditCommit(event -> commit(
                "Impossible de modifier la quantité du stock",
                () -> event.getRowValue().setQuantite(event.getNewValue())
        ));

        fixedColumn.setCellValueFactory(f -> {
            Stock stock = f.getValue();
            SimpleBooleanProperty fixed = new SimpleBooleanProperty(stock.estLimite());
            fixed.addListener((observable, oldValue, newValue) -> commit(
                    "Impossible de changer la limite du stock",
                    () -> stock.setLimite(newValue)
            ));
            return fixed;
        });
        fixedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(fixedColumn));

        consumedColumn.setCellValueFactory(f -> new SimpleStringProperty(consumption(f.getValue())));

        priceColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getPrix()));
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceColumn.setOnEditCommit(event -> commit(
                "Impossible de modifier le prix du stock",
                () -> event.getRowValue().setPrix(event.getNewValue())
        ));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setOnAction(e -> deleteStock(getTableView().getItems().get(getIndex())));
            }

            @Override
            public void updateItem(Void item, boolean isEmpty) {
                super.updateItem(item, isEmpty);
                setGraphic(isEmpty ? null : deleteButton);
            }
        });
        refreshTotals();
    }

    /**
     * Décrit ce que les tickets consomment d'un stock, et ce qui manque pour un stock non limité.
     *
     * @param stock le stock
     * @return la consommation, suivie du manque éventuel
     */
    private static String consumption(Stock stock) {
        long missing = stock.estLimite() ? 0 : stock.getQuantiteManquante();
        return missing > 0
                ? String.format("%d (manque %d)", stock.getQuantiteConsommee(), missing)
                : Long.toString(stock.getQuantiteConsommee());
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
        tableView.refresh();
        refreshTotals();
    }

    /**
     * Supprime un stock après confirmation s'il est utilisé par des tickets.
     *
     * @param stock le stock à supprimer
     */
    private void deleteStock(Stock stock) {
        if (!stock.getAvantages().isEmpty() && !confirm(
                "Supprimer le stock",
                String.format("Le stock « %s » est utilisé par %d avantage(s) de tickets, qui seront aussi supprimés. Continuer ?",
                        stock.getNom(), stock.getAvantages().size())
        ))
            return;
        tryCatch(
                "Impossible de supprimer le stock",
                () -> {
                    festival.supprimerStock(stock);
                    tableView.getItems().remove(stock);
                    refreshTotals();
                });
    }

    /**
     * Ajoute un stock à partir d'un formulaire.
     */
    @FXML
    private void addStock() {
        CustomValidField<RequiredTextField> stockName = new CustomValidField<>("Nom", new RequiredTextField());
        CustomValidField<IntTextField> quantity = new CustomValidField<>("Quantité", new IntTextField(true, true));
        CustomValidField<FloatTextField> price = new CustomValidField<>("Prix unitaire (€)", new FloatTextField(true, false));
        CustomValidField<CheckBox> fixed = new CustomValidField<>("Limité", new CheckBox("Les tickets ne peuvent pas dépasser cette quantité"));

        askValidForm(
                "Ajouter un stock",
                "Ajout d'un stock impossible",
                new CustomValidField<?>[]{stockName, quantity, price, fixed},
                () -> {
                    Stock stock = new Stock(
                            stockName.node.getText(),
                            quantity.node.getValue(),
                            fixed.node.isSelected(),
                            price.node.getValue()
                    );
                    festival.ajouterStock(stock);
                    tableView.getItems().add(stock);
                    refreshTotals();
                }
        );
    }

    /**
     * Affiche le coût total des stocks nécessaires et les stocks insuffisants.
     */
    private void refreshTotals() {
        total.setText(festival.getStocks().isEmpty() ? "" : String.format("Coût des stocks nécessaires : %.2f €", festival.getCoutStocks()));
        int insufficient = festival.getStocksInsuffisants().size();
        missing.setText(insufficient == 0 ? "" : String.format("%d stock(s) insuffisant(s) pour les tickets prévus", insufficient));
    }
}
