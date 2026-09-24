package cl.fersal.inventario.service;

import cl.fersal.inventario.dao.ProductoDAO;
import cl.fersal.inventario.model.Producto;
import javafx.application.Platform;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Exporta el inventario sin bloquear el hilo de JavaFX.
 */
public class ExportadorService {

    private static final String[] ENCABEZADOS = {
            "ID", "Código interno", "Categoría ID", "Nombre", "Dimensiones",
            "Unidad de medida", "Ubicación", "Stock actual", "Precio venta",
            "Precio venta por metro", "Precio trabajado por metro",
            "Costo promedio", "Usuario ID"
    };

    private final ProductoDAO productoDAO;

    public ExportadorService() {
        this(new ProductoDAO());
    }

    public ExportadorService(ProductoDAO productoDAO) {
        this.productoDAO = Objects.requireNonNull(productoDAO);
    }

    public void exportarCSVAsync(
            Path destino,
            Consumer<Path> alCompletar,
            Consumer<Exception> alFallar) {
        ejecutarEnSegundoPlano(
                () -> exportarCSV(destino),
                alCompletar,
                alFallar
        );
    }

    public void exportarExcelAsync(
            Path destino,
            Consumer<Path> alCompletar,
            Consumer<Exception> alFallar) {
        ejecutarEnSegundoPlano(
                () -> exportarExcel(destino),
                alCompletar,
                alFallar
        );
    }

    private Path exportarCSV(Path destino) throws IOException {
        List<Producto> productos = productoDAO.obtenerTodos();
        prepararArchivoDestino(destino);

        try (BufferedWriter escritor = Files.newBufferedWriter(
                destino,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            // BOM para que Excel reconozca UTF-8 al abrir el CSV directamente.
            escritor.write('\uFEFF');
            escribirFilaCSV(escritor, ENCABEZADOS);
            for (Producto producto : productos) {
                escribirFilaCSV(escritor, valoresProducto(producto));
            }
        }
        return destino;
    }

    private Path exportarExcel(Path destino) throws IOException {
        List<Producto> productos = productoDAO.obtenerTodos();
        prepararArchivoDestino(destino);

        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Inventario");
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);

            Row filaEncabezado = hoja.createRow(0);
            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                Cell celda = filaEncabezado.createCell(columna);
                celda.setCellValue(ENCABEZADOS[columna]);
                celda.setCellStyle(estiloEncabezado);
            }

            for (int indice = 0; indice < productos.size(); indice++) {
                Row fila = hoja.createRow(indice + 1);
                String[] valores = valoresProducto(productos.get(indice));

                for (int columna = 0; columna < valores.length; columna++) {
                    fila.createCell(columna).setCellValue(valores[columna]);
                }
            }

            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                hoja.autoSizeColumn(columna);
            }

            try (OutputStream salida = Files.newOutputStream(
                    destino,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                libro.write(salida);
            }
        }
        return destino;
    }

    private CellStyle crearEstiloEncabezado(Workbook libro) {
        Font fuente = libro.createFont();
        fuente.setBold(true);

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        return estilo;
    }

    private String[] valoresProducto(Producto producto) {
        return new String[]{
                texto(producto.getId()),
                texto(producto.getCodigoInterno()),
                texto(producto.getCategoriaId()),
                texto(producto.getNombre()),
                texto(producto.getDimensiones()),
                texto(producto.getUnidadMedida()),
                texto(producto.getUbicacion()),
                texto(producto.getStockActual()),
                texto(producto.getPrecioVenta()),
                texto(producto.getPrecioVentaMetro()),
                texto(producto.getPrecioTrabajadoMetro()),
                texto(producto.getCostoPromedio()),
                texto(producto.getUsuarioId())
        };
    }

    private void escribirFilaCSV(BufferedWriter escritor, String[] valores)
            throws IOException {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                escritor.write(',');
            }
            escritor.write(escaparCSV(valores[i]));
        }
        escritor.newLine();
    }

    private String escaparCSV(String valor) {
        String seguro = valor == null ? "" : valor;
        if (seguro.contains("\"") || seguro.contains(",")
                || seguro.contains("\r") || seguro.contains("\n")) {
            return "\"" + seguro.replace("\"", "\"\"") + "\"";
        }
        return seguro;
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private void prepararArchivoDestino(Path destino) throws IOException {
        Objects.requireNonNull(destino, "El destino de exportación es obligatorio.");
        Path padre = destino.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
    }

    private void ejecutarEnSegundoPlano(
            TareaExportacion tarea,
            Consumer<Path> alCompletar,
            Consumer<Exception> alFallar) {
        Objects.requireNonNull(alCompletar, "El callback de éxito es obligatorio.");
        Objects.requireNonNull(alFallar, "El callback de error es obligatorio.");

        Thread hilo = new Thread(() -> {
            try {
                Path resultado = tarea.ejecutar();
                Platform.runLater(() -> alCompletar.accept(resultado));
            } catch (Exception error) {
                Platform.runLater(() -> alFallar.accept(error));
            }
        }, "exportacion-inventario");

        hilo.setDaemon(true);
        hilo.start();
    }

    @FunctionalInterface
    private interface TareaExportacion {
        Path ejecutar() throws Exception;
    }
}
