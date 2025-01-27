module com.example.progetto_progiii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.google.gson;

    opens com.example.progetto_progiii to javafx.fxml;
    exports com.example.progetto_progiii;
    exports com.example.progetto_progiii.Controller;
    exports com.example.progetto_progiii.Model;
    opens com.example.progetto_progiii.Controller to javafx.fxml;
}