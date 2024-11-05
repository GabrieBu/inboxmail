module com.example.progetto_progiii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.progetto_progiii to javafx.fxml;
    exports com.example.progetto_progiii;
    exports com.example.progetto_progiii.Controller;
    opens com.example.progetto_progiii.Controller to javafx.fxml;
}