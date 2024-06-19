package com.musigma.controllers.workspaces;

import atlantafx.base.util.IntegerStringConverter;
import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Avantage;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.TypeTicket;
import com.musigma.models.exception.AvantageException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.TypeTicketException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

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
    TextField textFieldType, textFieldQuantite, textFieldPrix, textFieldAvantage;

    @FXML
    Button buttonTicket, buttonAvantage;


    @FXML
    TabPane tabPane;

    @FXML
    ComboBox<Stock> comboAvantage;


    @FXML
    public void initialize(Festival festival) throws FestivalException, AvantageException {
        super.initialize(festival);
        restoreTab();
        addListener();
        addToComboBox();
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

    private void addListener() {
        textFieldType.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals("Objet") || newValue.trim().isEmpty() || newValue.trim().isBlank() || newValue.matches(".*[^a-zA-Z-\\s].*")) {
                textFieldType.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldType.setStyle("-fx-border-color: transparent;");
            }
        });

        textFieldPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().isBlank() || !isNumeric(newValue.trim())) {
                textFieldPrix.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldPrix.setStyle("-fx-border-color: transparent;");
            }
        });

        textFieldQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || newValue.trim().isBlank() || !isNumeric(newValue.trim())) {
                textFieldQuantite.setStyle("-fx-border-color: crimson;");
            } else {
                textFieldQuantite.setStyle("-fx-border-color: transparent;");
            }
        });
    }

    private void onAddTicketPressed() throws FestivalException, TypeTicketException {
        textFieldType.setStyle("-fx-border-color: transparent;");
        textFieldPrix.setStyle("-fx-border-color: transparent;");
        textFieldQuantite.setStyle("-fx-border-color: transparent;");

        if (textFieldType.getText().trim().equals("Objet") || textFieldType.getText().trim().isEmpty() || textFieldType.getText().trim().isBlank() || textFieldType.getText().matches(".*[^a-zA-Z-\\s].*")) {
            textFieldType.requestFocus();
            textFieldType.setStyle("-fx-border-color: crimson;");
        } else if (textFieldPrix.getText().trim().isEmpty() || textFieldPrix.getText().trim().isBlank() || !isNumeric(textFieldPrix.getText().trim())) {
            textFieldPrix.requestFocus();
            textFieldPrix.setStyle("-fx-border-color: crimson;");
        } else if (textFieldQuantite.getText().trim().isEmpty() || textFieldQuantite.getText().trim().isBlank() || !isNumeric(textFieldQuantite.getText().trim())) {
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

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void createTab(TypeTicket ticket) {
        Tab newTab = new Tab(ticket.getType());
        TableView<Avantage> newTableView = new TableView<>();
        TableColumn<Avantage, String> avantageColumn = new TableColumn<>("Avantage");
        TableColumn<Avantage, Integer> quantityColumn = new TableColumn<>("Quantité");
        newTableView.getColumns().add(avantageColumn);
        newTableView.getColumns().add(quantityColumn);
        newTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        newTab.setContent(newTableView);
        avantageColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getStock().getName()));
        quantityColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getTicketType().getQuantity()));


        tabPane.getTabs().add(newTab);
    }

    private void restoreTab() throws AvantageException {
        for (int i = 0; i < festival.getTicketTypes().size(); i++) {
            createTab(festival.getTicketTypes().get(i));
            if(!festival.getTicketTypes().get(i).getAvantages().isEmpty()){

                TypeTicket ticket = festival.getTicketTypes().get(i).getAvantages().get(i).getTicketType();
                int quantity = festival.getTicketTypes().get(i).getAvantages().get(i).getQuantityByTicket();
                Stock stock = festival.getTicketTypes().get(i).getAvantages().get(i).getStock();
                Avantage avantage = new Avantage(ticket, stock, quantity);
                // Accéder à la tableview du tab actuel pour y ajouter l'avantage
                TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                tableView.getItems().add(avantage);
            }
        }
    }

    private void addToComboBox() {
        for (int i = 0; i < festival.getStocks().size(); i++) {
            comboAvantage.getItems().add(festival.getStocks().get(i));
        }
    }

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

    // Add avantage to the table view if an avantage is selected in the combo box and the quantity is not empty or blank
    private void onAddAvantagePressed() {
        if (comboAvantage.getSelectionModel().getSelectedItem() != null && !textFieldAvantage.getText().trim().isEmpty() && !textFieldAvantage.getText().trim().isBlank()) {
            TypeTicket ticket = festival.getTicketTypes().get(tabPane.getSelectionModel().getSelectedIndex());
            // Récupère la quantité dans le textFieldAvantage
            int quantity = Integer.parseInt(textFieldAvantage.getText());
            // Récupère le stock sélectionné dans la comboAvantage
            Stock stock = comboAvantage.getSelectionModel().getSelectedItem();
            try {
                Avantage avantage = new Avantage(ticket, stock, quantity);
                // Accéder à la tableview du tab actuel pour y ajouter l'avantage
                TableView<Avantage> tableView = (TableView<Avantage>) tabPane.getSelectionModel().getSelectedItem().getContent();
                tableView.getItems().add(avantage);
            } catch (AvantageException e) {
                e.printStackTrace();
            }
        } else {
            textFieldAvantage.requestFocus();
            textFieldAvantage.setStyle("-fx-border-color: crimson;");
        }
        textFieldAvantage.clear();
    }
}