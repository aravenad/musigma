package com.musigma.controllers.workspaces;

import atlantafx.base.util.DoubleStringConverter;
import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.FestivalException;
import impl.com.calendarfx.view.NumericTextField;
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
     * Initialise le contrôleur. Charge les stocks du festival dans la table. Définit les colonnes de la table. Définit les listeners pour les champs de saisie.
     * Définit un bouton de suppression pour chaque ligne de la table. Définit la modification des noms, quantités et prix des stocks.
     * @param festival le festival
     * @throws FestivalException si le festival est invalide
     */
    @FXML
    public void initialize(Festival festival) throws FestivalException, AvantageException {
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

        addListener();
        ajouterButton.setOnAction(e -> onAjouterPressed());
        addDeleteButtonToTable();
    }


    /**
     * Ajoute un bouton de suppression à la table, crée une colonne d'action avec le bouton de suppression.
     * Lorsque le bouton est cliqué, la ligne correspondante est supprimée de la table.
     * La méthode est appelée dans la méthode initialize.
     * @see #initialize(Festival)
     */
    private void addDeleteButtonToTable() {
        actionColumn = new TableColumn<>("Action");

        Callback<TableColumn<Stock, Void>, TableCell<Stock, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Stock, Void> call(final TableColumn<Stock, Void> param) {
                return new TableCell<>() {

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
            }
        };

        actionColumn.setCellFactory(cellFactory);
        tableView.getColumns().add(actionColumn);
    }


    /**
     * Définit les listeners pour les champs de saisie.
     * Si le champ est vide ou contient des caractères non autorisés, le champ est surligné en rouge.
     * Si le champ est valide, le surlignage est retiré.
     * La méthode est appelée dans la méthode initialize.
     * @see #initialize(Festival)
     * @see #onAjouterPressed()
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
     * Si les champs de saisie sont valides, un stock est créé et ajouté à la table.
     * Sinon, les champs de saisie invalides sont surlignés en rouge.
     * La méthode est appelée lorsqu'on clique sur le bouton "Ajouter".
     * @see #addListener()
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
