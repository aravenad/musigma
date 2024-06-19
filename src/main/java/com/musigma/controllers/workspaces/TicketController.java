package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Avantage;
import com.musigma.models.Festival;
import com.musigma.models.Stock;
import com.musigma.models.TypeTicket;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.StockException;
import com.musigma.models.exception.TypeTicketException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
    TextField textFieldType, textFieldQuantite, textFieldPrix;

    @FXML
    Button ajouterButton;

    @FXML
    Button update; //TODO: le bouton est casser depuis qu'on retire la tableView de base

    @FXML
    TabPane tabPane;

    @FXML
    TableView<String> tableView;

    @FXML
    TableColumn<Avantage, String> avantageColumn;

    @FXML
    TableColumn<Avantage, Integer> quantityColumn;

    @FXML
    TableColumn<Stock, Void> actionColumn;

    @FXML
    public void initialize(Festival festival) throws FestivalException {
        super.initialize(festival);
        restoreTab();
        addListener();
        ajouterButton.setOnAction(e -> {
            try {
                onAjouterPressed();
            } catch (TypeTicketException | FestivalException ex) {
                throw new RuntimeException(ex);
            }
        });

        configureTableView();

        for (TypeTicket ticket : festival.getTicketTypes()) {
            tableView.getColumns().add(new TableColumn<>(ticket.getType()));
        }
    }

    private void addListener() { //duplicata
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

    private void configureTableView() {
        if (tableView != null) {
            tableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Stock newRow = null;
                    try {
                        newRow = new Stock("Nouveau stock", 10, true, 10);
                    } catch (StockException e) {
                        throw new RuntimeException(e);
                    }
                    tableView.getItems().add("Nouveau stock");

                    System.out.println("Double clicked, new row added");
                }
            });
        } else {
            System.err.println("tableView is null. Please check your FXML file.");
        }
    }

    private void onAjouterPressed() throws FestivalException, TypeTicketException {
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

    private void createTab(TypeTicket ticket) throws FestivalException {
        Tab newTab = new Tab(ticket.getType());
        TableView<Stock> newTableView = new TableView<>();
        // ajouter 2 colonnes pour les avantages et les quantités
        TableColumn<Stock, Avantage> avantageColumn = new TableColumn<>("Avantage");
        TableColumn<Stock, Integer> quantityColumn = new TableColumn<>("Quantité");
        newTableView.getColumns().add(avantageColumn);
        newTableView.getColumns().add(quantityColumn);
        newTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        newTab.setContent(newTableView);

        // Event LIstener de l'autre
        newTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    newTableView.getItems().add(new Stock("Nouveau stock", 10, true, 10));
                    System.out.println("Double clicked, new row added to new table");
                    //TODO: demander a bastien et damien de faire en sorte que le texte s'affiche
                } catch (StockException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        tabPane.getTabs().add(newTab);
    }

    private void restoreTab() throws FestivalException {
        for (int i = 0; i < festival.getTicketTypes().size(); i++) {
            createTab(festival.getTicketTypes().get(i));
        }
    }
}
