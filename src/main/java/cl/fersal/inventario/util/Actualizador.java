package cl.fersal.inventario.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionException;

public class Actualizador {

    public static final String VERSION_LOCAL = "1.1.2"; // Cambiar manualmente después de cada nueva implementación
    private static final String URL_VERSION_REMOTA = "https://raw.githubusercontent.com/AdrianLezana/Fersal_Inventario/main/version.txt";
    private static final String URL_DESCARGA_RELEASE =
            "https://github.com/AdrianLezana/Fersal_Inventario/releases/download/";

    public static void verificarActualizaciones() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_VERSION_REMOTA))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        return;
                    }

                    String nuevaVersion = response.body().trim();
                    if (esVersionValida(nuevaVersion)
                            && !nuevaVersion.equals(VERSION_LOCAL)) {
                        Platform.runLater(() -> preguntarPorActualizacion(nuevaVersion));
                    }
                })
                .exceptionally(error -> {
                    System.err.println(
                            "No se pudo consultar la versión remota: "
                                    + mensajeError(error));
                    return null;
                });
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
        alertaDescarga.setHeaderText(null);
        alertaDescarga.setContentText(
                "Descargando actualización, por favor espere...");
        alertaDescarga.show();

        Path archivoTemporal = null;
        try {
            Path directorioTemporal = Path.of(
                    System.getProperty("java.io.tmpdir"));
            archivoTemporal = Files.createTempFile(
                    directorioTemporal,
                    "FersalInventario_Update_",
                    ".exe");

            URI uriDescarga = URI.create(
                    URL_DESCARGA_RELEASE
                            + "v"
                            + nuevaVersion
                            + "/FersalInventario-"
                            + nuevaVersion
                            + ".exe");
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriDescarga)
                    .timeout(Duration.ofMinutes(5))
                    .GET()
                    .build();

            Path destino = archivoTemporal;
            client.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofFile(destino))
                    .whenComplete((response, error) -> {
                        if (error != null) {
                            eliminarArchivoTemporal(destino);
                            Platform.runLater(() -> {
                                alertaDescarga.close();
                                mostrarError(
                                        "Error durante la descarga: "
                                                + mensajeError(error));
                            });
                            return;
                        }

                        if (response.statusCode() != 200) {
                            eliminarArchivoTemporal(destino);
                            Platform.runLater(() -> {
                                alertaDescarga.close();
                                mostrarError(
                                        "No se encontró el instalador en "
                                                + "GitHub. Código HTTP: "
                                                + response.statusCode());
                            });
                            return;
                        }

                        try {
                            if (Files.size(destino) == 0) {
                                throw new IOException(
                                        "GitHub devolvió un instalador vacío.");
                            }

                            Runtime.getRuntime().exec(
                                    new String[]{destino.toString()});

                            Platform.runLater(alertaDescarga::close);
                            Platform.exit();
                            System.exit(0);
                        } catch (IOException | SecurityException e) {
                            eliminarArchivoTemporal(destino);
                            Platform.runLater(() -> {
                                alertaDescarga.close();
                                mostrarError(
                                        "No se pudo iniciar el instalador: "
                                                + e.getMessage());
                            });
                        }
                    });
        } catch (IOException | IllegalArgumentException e) {
            if (archivoTemporal != null) {
                eliminarArchivoTemporal(archivoTemporal);
            }
            alertaDescarga.close();
            mostrarError(
                    "No se pudo preparar la descarga: " + e.getMessage());
        }
    }

    private static boolean esVersionValida(String version) {
        return version != null
                && version.matches(
                        "v?\\d+(\\.\\d+){1,3}([+-][A-Za-z0-9.-]+)?");
    }

    private static void eliminarArchivoTemporal(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            System.err.println(
                    "No se pudo eliminar el instalador temporal: "
                            + e.getMessage());
        }
    }

    private static String mensajeError(Throwable error) {
        Throwable causa = error instanceof CompletionException
                && error.getCause() != null
                ? error.getCause()
                : error;
        return causa.getMessage() == null
                ? causa.getClass().getSimpleName()
                : causa.getMessage();
    }

    private static void mostrarError(String msj) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Fallo en la actualización");
        alert.setContentText(msj);
        alert.show();
    }
}