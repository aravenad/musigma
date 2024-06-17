package com.musigma.controllers;

public class CalendarController extends WorkspaceController {
    public static WorkspaceController.WorkspaceRegister REGISTER = new WorkspaceController.WorkspaceRegister(
            "Calendar",
            "/com/musigma/images/icons/calendar.png",
            "/com/musigma/views/calendar-view.fxml"
    );
}
