package cl.fersal.inventario.service;

import cl.fersal.inventario.config.ConexionDB;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Gestiona los respaldos locales de la base de datos SQLite.
 */
public class RespaldoService {

    private static final String NOMBRE_CARPETA_RESPALDOS = "Respaldos";
    private static final String PATRON_RESPALDO = "inventario_fersal_*.db";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int DIAS_RETENCION = 30;

    /**
     * Copia la base de datos actual y elimina respaldos con más de 30 días.
     *
     * @return ruta del respaldo creado
     * @throws IOException si no se puede crear, copiar o limpiar un respaldo
     */
    public Path crearRespaldo() throws IOException {
        Path rutaBaseDatos = ConexionDB.obtenerRutaBaseDatos();
        if (!Files.isRegularFile(rutaBaseDatos)) {
            throw new IOException("No se encontró la base de datos en: " + rutaBaseDatos);
        }

        Path carpetaRespaldos = rutaBaseDatos.getParent().resolve(NOMBRE_CARPETA_RESPALDOS);
        Files.createDirectories(carpetaRespaldos);

        String marcaTiempo = LocalDateTime.now().format(FORMATO_FECHA);
        Path rutaRespaldo = carpetaRespaldos.resolve(
                "inventario_fersal_" + marcaTiempo + ".db");

        Files.copy(rutaBaseDatos, rutaRespaldo, StandardCopyOption.REPLACE_EXISTING);
        eliminarRespaldosAntiguos(carpetaRespaldos);

        return rutaRespaldo;
    }

    private void eliminarRespaldosAntiguos(Path carpetaRespaldos) throws IOException {
        Instant fechaLimite = Instant.now().minus(Duration.ofDays(DIAS_RETENCION));

        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(
                carpetaRespaldos, PATRON_RESPALDO)) {
            for (Path archivo : archivos) {
                if (!Files.isRegularFile(archivo)) {
                    continue;
                }

                Instant ultimaModificacion = Files.getLastModifiedTime(archivo).toInstant();
                if (ultimaModificacion.isBefore(fechaLimite)) {
                    Files.deleteIfExists(archivo);
                }
            }
        }
    }
}
