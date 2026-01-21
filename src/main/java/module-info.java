module com.espoch.grafo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.espoch.grafo to javafx.fxml;

    exports com.espoch.grafo;
    exports com.espoch.grafo.controller;
    opens com.espoch.grafo.controller to javafx.fxml;
    exports com.espoch.grafo.model;
    opens com.espoch.grafo.model to javafx.fxml;
    exports com.espoch.grafo.view;
    opens com.espoch.grafo.view to javafx.fxml;
}