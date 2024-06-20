module com.musigma {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires java.desktop;
    requires atlantafx.base;
    requires com.calendarfx.view;
    requires java.rmi;
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