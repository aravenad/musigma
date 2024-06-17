package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;

public class TicketController extends WorkspaceController {
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Ticket",
            "/com/musigma/images/icons/ticket.png",
            "/com/musigma/views/ticket-view.fxml"
    );
}
