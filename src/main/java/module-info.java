module com.example.inventariofersal {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens cl.fersal.inventario to javafx.fxml;
    exports cl.fersal.inventario;
}