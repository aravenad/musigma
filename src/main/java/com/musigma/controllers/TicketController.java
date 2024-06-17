package com.musigma.controllers;

public class TicketController extends WorkspaceController {
    public static WorkspaceController.WorkspaceRegister REGISTER = new WorkspaceController.WorkspaceRegister(
            "Ticket",
            "/com/musigma/images/icons/ticket.png",
            "/com/musigma/views/ticket-view.fxml"
    );
}
