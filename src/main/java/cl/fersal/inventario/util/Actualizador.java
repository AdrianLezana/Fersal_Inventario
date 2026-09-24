package cl.fersal.inventario.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class Actualizador {

    public static final String VERSION_LOCAL = "1.0"; // Cambiar manualmente después de cada nueva implementación
    private static final String URL_VERSION_REMOTA = "https://raw.githubusercontent.com/AdrianLezana/Fersal_Inventario/main/version.txt";

    public static void verificarActualizaciones() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_VERSION_REMOTA)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(versionRemota -> {
                    String nuevaVersion = versionRemota.trim();
                    if (!nuevaVersion.isEmpty() && !nuevaVersion.equals(VERSION_LOCAL)) {
                        Platform.runLater(() -> preguntarPorActualizacion(nuevaVersion));
                    }
                })
                .exceptionally(e -> null); // Falla en silencio si no hay internet
    }

    private static void preguntarPorActualizacion(String nuevaVersion) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Actualización Disponible");
        alerta.setHeaderText("Versión " + nuevaVersion + " disponible");
        alerta.setContentText("¿Desea descargar e instalar la actualización ahora?\nEl programa se cerrará automáticamente durante el proceso.");

        alerta.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                descargarEInstalar(nuevaVersion);
            }
        });
    }

    private static void descargarEInstalar(String nuevaVersion) {
        Alert alertaDescarga = new Alert(Alert.AlertType.INFORMATION);
        alertaDescarga.setTitle("Actualizando...");
        alertaDescarga.setHeaderText("Descargando actualización");
        alertaDescarga.setContentText("Por favor, espere. Descargando desde GitHub...");
        alertaDescarga.show();

        new Thread(() -> {
            try {
                // URL predecible donde buscará el archivo dentro de las "Releases" de GitHub
                String urlExe = "https://github.com/AdrianLezana/Fersal_Inventario/releases/download/" + nuevaVersion + "/FersalInventario.exe";

                // followRedirects es OBLIGATORIO para descargar binarios desde GitHub
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlExe)).GET().build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    Path rutaTemporal = Path.of(System.getProperty("java.io.tmpdir"), "FersalInventario_Update.exe");
                    Files.copy(response.body(), rutaTemporal, StandardCopyOption.REPLACE_EXISTING);

                    // Ordenamos a Windows ejecutar el instalador temporal
                    new ProcessBuilder(rutaTemporal.toString()).start();

                    // Suicidio del proceso Java actual para soltar los permisos de la carpeta
                    System.exit(0);
                } else {
                    Platform.runLater(() -> {
                        alertaDescarga.close();
                        mostrarError("No se encontró el instalador en GitHub. Código HTTP: " + response.statusCode());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    alertaDescarga.close();
                    mostrarError("Error de conexión durante la descarga: " + e.getMessage());
                });
            }
        }).start();
    }

    private static void mostrarError(String msj) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Fallo en la actualización");
        alert.setContentText(msj);
        alert.show();
    }
}