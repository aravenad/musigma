package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.StockException;
import impl.com.calendarfx.view.NumericTextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import static com.musigma.utils.Dialogs.tryCatch;

/**
 * Contrôleur pour l'espace de travail Stock.
 */
public class StockController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Stock",
            "/com/musigma/images/icons/stock.png",
            "/com/musigma/views/stock-view.fxml"
    );

    @FXML
    TextField textFieldObjet;

    @FXML
    NumericTextField textFieldQuantite, textFieldPrix;

    @FXML
    Button ajouterButton;

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
    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize(Festival festival) throws FestivalException {
        super.initialize(festival);
        tableView.getItems().addAll(festival.getStocks());
        addListener();
        ajouterButton.setOnAction(e -> onAjouterPressed()
        );
        nameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getName()));
        quantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getQuantity()));
        priceColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getPrix()));
        addDeleteButtonToTable();
    }
    /**
     * Ajoute un bouton de suppression à la table, crée une colonne d'action avec le bouton de suppression.
     */
    private void addDeleteButtonToTable() {
        actionColumn = new TableColumn<>("Action");

        Callback<TableColumn<Stock, Void>, TableCell<Stock, Void>> cellFactory = new Callback<TableColumn<Stock, Void>, TableCell<Stock, Void>>() {
            @Override
            public TableCell<Stock, Void> call(final TableColumn<Stock, Void> param) {
                final TableCell<Stock, Void> cell = new TableCell<>() {

                    private final Button btn = new Button("Supprimer");

                    {
                        btn.setOnAction((e) -> {
                            Stock stock = getTableView().getItems().get(getIndex());
                            getTableView().getItems().remove(stock);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
        tableView.getColumns().add(actionColumn);
    }


    /**
     * Définit les listeners pour les champs de saisie.
     */
    private void addListener(){
        textFieldObjet.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals("Objet") || newValue.trim().isEmpty() || newValue.trim().isBlank() || newValue.matches(".*[^a-zA-Z-\\s].*")) {
                textFieldObjet.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldObjet.setStyle("-fx-border-color: transparent;");
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
     * Ajoute un stock à la table.
     *
     * @throws StockException si le stock n'est pas valide
     */
    private void onAjouterPressed() {
        textFieldObjet.setStyle("-fx-border-color: transparent;");
        textFieldPrix.setStyle("-fx-border-color: transparent;");
        textFieldQuantite.setStyle("-fx-border-color: transparent;");

        if (textFieldObjet.getText().trim().equals("Objet") || textFieldObjet.getText().trim().isEmpty() || textFieldObjet.getText().trim().isBlank() || textFieldObjet.getText().matches(".*[^a-zA-Z-\\s].*")) {
            textFieldObjet.requestFocus();
            textFieldObjet.setStyle("-fx-border-color: crimson;");
        } else if (textFieldPrix.getText().trim().isEmpty() || textFieldPrix.getText().trim().isBlank()) {
            textFieldPrix.requestFocus();
            textFieldPrix.setStyle("-fx-border-color: crimson;");
        } else if (textFieldQuantite.getText().trim().isEmpty() || textFieldPrix.getText().trim().isBlank()) {
            textFieldQuantite.requestFocus();
            textFieldQuantite.setStyle("-fx-border-color: crimson;");
        } else {
            tryCatch(
        "Ajout du stock impossible",
                () -> {
                    Stock stock = new Stock(
                        textFieldObjet.getText(),
                            Integer.parseInt(textFieldQuantite.getText()),
                            true,
                            Double.parseDouble(textFieldPrix.getText()));
                    festival.addStock(stock);
                    tableView.getItems().add(stock);
                    textFieldObjet.setStyle("-fx-border-color: transparent;");
                    textFieldPrix.setStyle("-fx-border-color: transparent;");
                    textFieldQuantite.setStyle("-fx-border-color: transparent;");
                }
            );
        }
    }
}
