package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Bilan;
import com.musigma.models.Festival;
import com.musigma.models.PlanDeVente;
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
    private TableView<Bilan.Ligne> revenuesTable, costsTable;

    @FXML
    private TableColumn<Bilan.Ligne, String> revenueLabelColumn, revenueAmountColumn, costLabelColumn, costAmountColumn;

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
    private PlanDeVente plan;

    /**
     * Initialisation de l'espace de travail : bilan et indicateurs du festival.
     *
     * @param festival le festival
     */
    @Override
    public void initialize(Festival festival) {
        super.initialize(festival);
        revenueLabelColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().libelle()));
        revenueAmountColumn.setCellValueFactory(f -> new SimpleStringProperty(CURRENCY.format(f.getValue().montant())));
        costLabelColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().libelle()));
        costAmountColumn.setCellValueFactory(f -> new SimpleStringProperty(CURRENCY.format(f.getValue().montant())));
        planTicketColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getType()));
        planCurrentColumn.setCellValueFactory(f -> new SimpleStringProperty(Integer.toString(f.getValue().getQuantite())));
        planRecommendedColumn.setCellValueFactory(f -> new SimpleStringProperty(
                plan == null ? "" : Integer.toString(plan.quantites().get(f.getValue()))
        ));
        refresh();
    }

    /**
     * Affiche le bilan prévisionnel et les indicateurs du festival.
     */
    private void refresh() {
        Bilan budget = festival.getBilan();
        revenuesTable.getItems().setAll(budget.recettes());
        costsTable.getItems().setAll(budget.charges());
        result.setText(String.format("Résultat prévisionnel : %s (recettes %s, charges %s)",
                CURRENCY.format(budget.resultat()), CURRENCY.format(budget.totalRecettes()), CURRENCY.format(budget.totalCharges())));
        result.setStyle(budget.resultat() < 0 ? "-fx-text-fill: -color-danger-fg;" : "-fx-text-fill: -color-success-fg;");

        attendance.setText(String.format("Personnes attendues : %d", festival.getFrequentationAttendue()));
        surface.setText(String.format("Surface occupée : %.0f m² sur %.0f m²", festival.getSurfaceOccupee(), festival.getSuperficie()));
        surface.setStyle(festival.getSurfaceOccupee() > festival.getSuperficie() ? "-fx-text-fill: -color-danger-fg;" : "");
        security.setText(String.format("Agents de sécurité recommandés : %d", festival.getAgentsSecuriteRecommandes()));

        planTable.getItems().setAll(plan == null ? java.util.List.of() : plan.quantites().keySet());
        planTable.refresh();
        planRevenue.setText(plan == null ? "" : "Recette de la vente optimale : " + CURRENCY.format(plan.recette()));
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
                    plan = festival.optimiserVentes();
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
                    festival.appliquerPlanDeVente(plan);
                    refresh();
                }
        );
    }
}
