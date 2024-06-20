package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import com.musigma.models.exception.TypeTicketException;
import javafx.fxml.FXML;
import javafx.scene.control.*;


/**
 * Contrôleur pour l'espace de travail Ticket.
 */
public class AccountingController extends WorkspaceController {
    /**
     * Enregistrement de l'espace de travail.
     */
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Prévision",
            "/com/musigma/images/icons/accounting.png",
            "/com/musigma/views/accounting-view.fxml"
    );

    @FXML
    Label labelValue, labelCapa, labelSecu;

    @FXML
    public void initialize(Festival festival) throws TypeTicketException {
        super.initialize(festival);
        labelValue.setText(String.valueOf(festival.optimizeResult()));
        labelCapa.setText(String.valueOf(getRecommandedPerson()) + " personnes");
        labelSecu.setText(String.valueOf(getRecommandedSecurity()) + " agents de sécurité");
    }

    public int getRecommandedPerson() {
        return (int) (festival.getArea() / 0.42);
    }

    public int getRecommandedSecurity() {
        return Math.max(3, (int) (festival.getArea() / 100));
    }

}