package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Budget;
import com.musigma.models.Festival;
import com.musigma.models.SalesPlan;
import com.musigma.models.TypeTicket;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.Locale;

import static com.musigma.controllers.Dialogs.tryCatch;

/**
 * Contrôleur pour l'espace de travail Prévision (UC3.2) : bilan prévisionnel et vente optimale des tickets.
 */
public class AccountingController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static final WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Prévision",
            "/com/musigma/images/icons/accounting.png",
            "/com/musigma/views/accounting-view.fxml",
            "Consultez le bilan prévisionnel. « Calculer la vente optimale » propose les quantités de tickets qui maximisent la recette, « Appliquer » les reporte dans les tickets."
    );

    /**
     * Format des montants, ex : « 205 000,00 € ».
     */
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    @FXML
    private TableView<Budget.Line> revenuesTable, costsTable;

    @FXML
    private TableColumn<Budget.Line, String> revenueLabelColumn, revenueAmountColumn, costLabelColumn, costAmountColumn;

    @FXML
    private TableView<TypeTicket> planTable;

    @FXML
    private TableColumn<TypeTicket, String> planTicketColumn, planCurrentColumn, planRecommendedColumn;

    @FXML
    private Label result, planRevenue, attendance, surface, security;

    @FXML
    private Button applyButton;

    /**
     * Dernière recommandation calculée, ou null.
     */
    private SalesPlan plan;

    /**
     * Initialisation de l'espace de travail : bilan et indicateurs du festival.
     *
     * @param festival le festival
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        revenueLabelColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().label()));
        revenueAmountColumn.setCellValueFactory(f -> new SimpleStringProperty(CURRENCY.format(f.getValue().amount())));
        costLabelColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().label()));
        costAmountColumn.setCellValueFactory(f -> new SimpleStringProperty(CURRENCY.format(f.getValue().amount())));
        planTicketColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getType()));
        planCurrentColumn.setCellValueFactory(f -> new SimpleStringProperty(Integer.toString(f.getValue().getQuantity())));
        planRecommendedColumn.setCellValueFactory(f -> new SimpleStringProperty(
                plan == null ? "" : Integer.toString(plan.quantities().get(f.getValue()))
        ));
        refresh();
    }

    /**
     * Affiche le bilan prévisionnel et les indicateurs du festival.
     */
    private void refresh() {
        Budget budget = festival.getBudget();
        revenuesTable.getItems().setAll(budget.revenues());
        costsTable.getItems().setAll(budget.costs());
        result.setText(String.format("Résultat prévisionnel : %s (recettes %s, charges %s)",
                CURRENCY.format(budget.result()), CURRENCY.format(budget.totalRevenue()), CURRENCY.format(budget.totalCosts())));
        result.setStyle(budget.result() < 0 ? "-fx-text-fill: -color-danger-fg;" : "-fx-text-fill: -color-success-fg;");

        attendance.setText(String.format("Personnes attendues : %d", festival.getExpectedAttendance()));
        surface.setText(String.format("Surface occupée : %.0f m² sur %.0f m²", festival.getOccupiedSurface(), festival.getArea()));
        surface.setStyle(festival.getOccupiedSurface() > festival.getArea() ? "-fx-text-fill: -color-danger-fg;" : "");
        security.setText(String.format("Agents de sécurité recommandés : %d", festival.getRecommendedSecurityAgents()));

        planTable.getItems().setAll(plan == null ? java.util.List.of() : plan.quantities().keySet());
        planTable.refresh();
        planRevenue.setText(plan == null ? "" : "Recette de la vente optimale : " + CURRENCY.format(plan.revenue()));
        applyButton.setDisable(plan == null);
    }

    /**
     * Calcule la vente optimale des tickets sans modifier le festival.
     */
    @FXML
    private void optimize() {
        tryCatch(
                "Calcul de la vente optimale impossible",
                () -> {
                    plan = festival.optimizeSales();
                    refresh();
                }
        );
    }

    /**
     * Applique la vente optimale aux quantités de tickets, puis met le bilan à jour.
     */
    @FXML
    private void applyPlan() {
        if (plan == null)
            return;
        tryCatch(
                "Application de la recommandation impossible",
                () -> {
                    festival.applySalesPlan(plan);
                    refresh();
                }
        );
    }
}
