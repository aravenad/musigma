/**
 * Le module com.musigma est le module principal de l'application.
 */
module com.musigma {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.logging;
    requires atlantafx.base;
    requires com.calendarfx.view;
    requires ojalgo;

    opens com.musigma to javafx.fxml;
    exports com.musigma;
    exports com.musigma.models.exception;
    exports com.musigma.controllers;
    opens com.musigma.controllers to javafx.fxml;
    opens com.musigma.controllers.components to javafx.fxml;
    exports com.musigma.controllers.workspaces;
    opens com.musigma.controllers.workspaces to javafx.fxml;
    exports com.musigma.utils;
    opens com.musigma.utils to javafx.fxml;
    exports com.musigma.utils.exceptionMethods;
    opens com.musigma.utils.exceptionMethods to javafx.fxml;
}