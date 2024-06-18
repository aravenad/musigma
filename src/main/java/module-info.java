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

    opens com.musigma to javafx.fxml;
    exports com.musigma;
    exports com.musigma.controllers;
    opens com.musigma.controllers to javafx.fxml;
    exports com.musigma.controllers.workspaces;
    opens com.musigma.controllers.workspaces to javafx.fxml;
}