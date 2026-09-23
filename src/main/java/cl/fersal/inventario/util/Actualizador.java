package cl.fersal.inventario.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Actualizador {

    // La versión actual de este .exe (Debes cambiarlo manualmente antes de compilar una nueva versión)
    public static final String VERSION_LOCAL = "1.0";

    // La URL "raw" de tu archivo en GitHub (ajusta tu usuario/repositorio si es necesario)
    private static final String URL_VERSION_REMOTA = "https://raw.githubusercontent.com/AdrianLezana/Fersal_Inventario/main/version.txt";
    private static final String URL_DESCARGA = "https://github.com/AdrianLezana/Fersal_Inventario/releases";

    public static void verificarActualizaciones() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // Si no hay internet, se rinde en 5 segundos
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_VERSION_REMOTA))
                .GET()
                .build();

        // Enviamos la petición en segundo plano para no congelar el Login
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(versionRemota -> {
                    String versionLimpia = versionRemota.trim(); // Quitamos espacios o saltos de línea

                    if (!versionLimpia.isEmpty() && !versionLimpia.equals(VERSION_LOCAL)) {
                        // Si las versiones no coinciden, mostramos la alerta (obligando a usar el hilo de JavaFX)
                        Platform.runLater(() -> mostrarAlertaActualizacion(versionLimpia));
                    } else {
                        System.out.println("El sistema está actualizado. Versión local: " + VERSION_LOCAL);
                    }
                })
                .exceptionally(e -> {
                    System.err.println("No se pudo verificar la actualización. ¿Sin internet? " + e.getMessage());
                    return null;
                });
    }

    private static void mostrarAlertaActualizacion(String nuevaVersion) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Actualización Disponible");
        alerta.setHeaderText("¡Hay una nueva versión de Fersal Inventario!");

        Label mensaje = new Label("Versión actual: " + VERSION_LOCAL + "\nNueva versión: " + nuevaVersion + "\n\nPor favor, descargue la actualización desde el repositorio:");

        // Creamos un link cliqueable
        Hyperlink link = new Hyperlink(URL_DESCARGA);
        link.setOnAction(e -> {
            try {
                // Esto abre el navegador por defecto del usuario
                java.awt.Desktop.getDesktop().browse(new URI(URL_DESCARGA));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox contenido = new VBox(mensaje, link);
        alerta.getDialogPane().setContent(contenido);
        alerta.show();
    }
}