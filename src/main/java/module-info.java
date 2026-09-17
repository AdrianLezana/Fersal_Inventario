module com.example.inventariofersal {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.inventariofersal to javafx.fxml;
    exports com.example.inventariofersal;
}