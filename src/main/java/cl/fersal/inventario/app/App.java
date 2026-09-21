package cl.fersal.inventario.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    // Guardamos la ventana principal para poder cambiar su contenido después
    private static Stage escenarioPrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        escenarioPrincipal = stage;

        // La ruta debe coincidir exactamente con la ubicación en la carpeta resources
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/cl/fersal/inventario/fxml/login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 350, 300);

        stage.setTitle("Fersal Inventario - Acceso");
        stage.setScene(scene);
        stage.setResizable(false); // Bloqueamos el redimensionamiento para el login
        stage.show();
    }

    /**
     * Método global para cambiar de pantallas sin abrir múltiples ventanas.
     */
    public static void cambiarEscena(String rutaFxml, String titulo, int ancho, int alto) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(rutaFxml));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, ancho, alto);

        escenarioPrincipal.setTitle(titulo);
        escenarioPrincipal.setScene(scene);
    }

    public static void main(String[] args) {
        // 1. Verificar y construir la base de datos si está vacía
        cl.fersal.inventario.config.ConexionDB.inicializarBaseDeDatos();

        // 2. Crear el usuario administrador por defecto si no existe
        cl.fersal.inventario.dao.UsuarioDAO dao = new cl.fersal.inventario.dao.UsuarioDAO();
        if (dao.obtenerPorUsername("admin") == null) {
            cl.fersal.inventario.model.Usuario admin = new cl.fersal.inventario.model.Usuario("admin", "123456");
            dao.crear(admin);
            System.out.println("Usuario 'admin' creado en la base de datos.");
        }

        // 3. Arrancar la interfaz gráfica
        launch(args);
    }
}