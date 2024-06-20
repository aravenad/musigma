package com.musigma.controllers.workspaces;

import atlantafx.base.util.DoubleStringConverter;
import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.components.FloatTextField;
import com.musigma.controllers.components.IntTextField;
import com.musigma.controllers.components.RequiredTextField;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.TypeTicketException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import static com.musigma.utils.Dialogs.tryCatch;

/**
 * Contrôleur pour l'espace de travail Stock.
 */
public class StockController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     * Définit le nom, l'icône et la vue de l'espace de travail.
     * @see com.musigma.controllers.WorkspaceController
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Stock",
            "/com/musigma/images/icons/stock.png",
            "/com/musigma/views/stock-view.fxml"
    );

    @FXML
    RequiredTextField textFieldObjet;

    @FXML
    IntTextField textFieldQuantite;

    @FXML
    FloatTextField textFieldPrix;

    @FXML
    Button buttonStock;

    @FXML
    TableView<Stock> tableView;

    @FXML
    TableColumn<Stock, String> nameColumn;

    @FXML
    TableColumn<Stock, Integer> quantityColumn;

    @FXML
    TableColumn<Stock, Double> priceColumn;

    @FXML
    TableColumn<Stock, Void> actionColumn;

    @FXML
    Label total;

    /**
     * Initialise le contrôleur. Charge les stocks du festival dans la table. Définit les colonnes de la table. Définit les listeners pour les champs de saisie.
     * Définit un bouton de suppression pour chaque ligne de la table. Définit la modification des noms, quantités et prix des stocks.
     * @param festival le festival
     * @throws FestivalException si le festival est invalide
     */
    @FXML
    public void initialize(Festival festival) {
        super.initialize(festival);
        tableView.getItems().addAll(festival.getStocks());
        tableView.setEditable(true); // Permet l'édition de la TableView

        nameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getName()));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Permet l'édition des noms
        nameColumn.setOnEditCommit(event -> {
            Stock stock = event.getRowValue();
            tryCatch(
                "Impossible de modifier le nom du stock",
                () -> stock.setName(event.getNewValue()));
        });

        quantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantity()));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter())); // Permet l'édition des quantités
        quantityColumn.setOnEditCommit(event -> {
            Stock stock = event.getRowValue();
            tryCatch(
                "Impossible de modifier la quantité du stock",
                () -> stock.setQuantity(event.getNewValue()));
        });

        priceColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getPrix()));
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter())); // Permet l'édition des prix
        priceColumn.setOnEditCommit(event -> {
            Stock stock = event.getRowValue();
            tryCatch(
                "Impossible de modifier le prix du stock",
                () -> stock.setPrix(event.getNewValue()));
        });
        buttonStock.setOnAction(e -> onAddStockPressed());
        addDeleteButtonToTable();
    }

    /**
     * Ajoute un bouton de suppression à la table, crée une colonne d'action avec le bouton de suppression.
     * Lorsque le bouton est cliqué, la ligne correspondante est supprimée de la table.
     * La méthode est appelée dans la méthode initialize.
     * @see #initialize(Festival)
     */
    private void addDeleteButtonToTable() {
        actionColumn = new TableColumn<>("Actions");
        Callback<TableColumn<Stock, Void>, TableCell<Stock, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Stock, Void> call(final TableColumn<Stock, Void> param) {
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
                                Stock stock = getTableView().getItems().get(getIndex());
                                tryCatch(
                            "Impossible de supprimer le stock",
                                    () -> {
                                        festival.removeStock(stock);
                                        getTableView().getItems().remove(stock);
                                        totalPrix();
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
     * Ajoute un stock à la table.
     * Si les champs de saisie sont valides, un stock est créé et ajouté à la table.
     * Sinon, les champs de saisie invalides sont surlignés en rouge.
     * La méthode est appelée lorsqu'on clique sur le bouton "Ajouter".
     */
    private void onAddStockPressed() {
        if (!textFieldObjet.isValid()) {
            textFieldObjet.requestFocus();
        } else if (!textFieldQuantite.isValid()) {
            textFieldQuantite.requestFocus();
        } else if (!textFieldPrix.isValid()) {
            textFieldPrix.requestFocus();
        } else tryCatch(
                "Ajout du stock impossible",
                () -> {
                    Stock stock = new Stock(
                        textFieldObjet.getText(),
                        textFieldQuantite.getValue(),
                        true,
                        textFieldPrix.getValue());
                    festival.addStock(stock);
                    tableView.getItems().add(stock);
                    totalPrix();
                }
        );
    }

    /**
     * Calcule le prix total des stocks.
     */
    private void totalPrix(){
        double t = 0;
        for (Stock stock : tableView.getItems()) {
            t += stock.getPrix() * stock.getQuantity();
        }
        total.setText("Total : " + t + " €");
    }
}