module cl.fersal.inventario {
    // Librerías gráficas
    requires javafx.controls;
    requires javafx.fxml;

    // Librerías de terceros (Base de datos SQLite)
    requires java.sql;

    // 1. Exportamos el modelo para que otras librerías sepan que existe
    exports cl.fersal.inventario.model;

    // 2. Le damos permiso a javafx.base (que maneja las tablas) para leer los Getters de tus clases
    opens cl.fersal.inventario.model to javafx.base;

    // Le decimos a Java qué carpetas pueden ser ejecutadas
    exports cl.fersal.inventario.app;

    // Le damos permiso a JavaFX para "entrar" a la carpeta controller
    // y conectar los botones del FXML con tus métodos @FXML
    opens cl.fersal.inventario.controller to javafx.fxml;
}