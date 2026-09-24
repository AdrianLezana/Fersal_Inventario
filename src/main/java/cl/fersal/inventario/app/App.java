package cl.fersal.inventario.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import cl.fersal.inventario.service.RespaldoService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class App extends Application {

    // Guardamos la ventana principal para poder cambiar su contenido después
    private static Stage escenarioPrincipal;
    private final RespaldoService respaldoService = new RespaldoService();
    private final AtomicBoolean cierreAutorizado = new AtomicBoolean(false);
    private final AtomicBoolean respaldoEnCurso = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) throws IOException {
        escenarioPrincipal = stage;
        configurarCierre(stage);

        // La ruta debe coincidir exactamente con la ubicación en la carpeta resources
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/cl/fersal/inventario/fxml/login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 350, 300);

        stage.setTitle("Fersal Inventario - Acceso");
        stage.setScene(scene);
        stage.setResizable(false); // Bloqueamos el redimensionamiento para el login
        stage.show();
    }

    private void configurarCierre(Stage stage) {
        stage.setOnCloseRequest(evento -> {
            if (cierreAutorizado.get()) {
                return;
            }

            evento.consume();
            if (!respaldoEnCurso.compareAndSet(false, true)) {
                return;
            }

            new Thread(() -> {
                try {
                    Path rutaRespaldo = respaldoService.crearRespaldo();
                    System.out.println("Respaldo creado en: " + rutaRespaldo);
                    cierreAutorizado.set(true);
                    Platform.runLater(stage::close);
                } catch (IOException e) {
                    respaldoEnCurso.set(false);
                    Platform.runLater(() -> mostrarErrorRespaldo(stage, e));
                }
            }, "respaldo-al-salir").start();
        });
    }

    private void mostrarErrorRespaldo(Stage stage, IOException error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(stage);
        alerta.setTitle("Error al cerrar");
        alerta.setHeaderText("No se pudo crear el respaldo");
        alerta.setContentText(
                "El programa permanecerá abierto para evitar salir sin respaldo.\n\n"
                        + error.getMessage());
        alerta.showAndWait();
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