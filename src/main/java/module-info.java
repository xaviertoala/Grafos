module com.espoch.grafo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.espoch.grafo to javafx.fxml;

    exports com.espoch.grafo;
}