package cl.fersal.inventario.controller;

import cl.fersal.inventario.app.App;
import cl.fersal.inventario.dao.ProductoDAO;
import cl.fersal.inventario.model.Producto;
import cl.fersal.inventario.service.ExportadorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DashboardController {

    @FXML private Label lblUsuarioActual;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDimensiones;
    @FXML private TableColumn<Producto, Double> colStock;
    @FXML private TableColumn<Producto, String> colMedida;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Double> colCosto;
    @FXML private TableColumn<Producto, Double> colPrecioMetro;
    @FXML private TableColumn<Producto, Double> colPrecioTrabajado;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ExportadorService exportadorService = new ExportadorService();
    private ObservableList<Producto> listaProductos;

    @FXML
    public void initialize() {
        // Enlazar columnas con los atributos de la clase Producto
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoInterno"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDimensiones.setCellValueFactory(new PropertyValueFactory<>("dimensiones"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colMedida.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoPromedio"));
        colPrecioMetro.setCellValueFactory(new PropertyValueFactory<>("precioVentaMetro"));
        colPrecioTrabajado.setCellValueFactory(new PropertyValueFactory<>("precioTrabajadoMetro"));

        cargarDatos();
    }

    private void cargarDatos() {
        // 1. Obtenemos los datos desde la base de datos
        List<Producto> productosBD = productoDAO.obtenerTodos();
        listaProductos = FXCollections.observableArrayList(productosBD);

        // 2. En lugar de pasar la lista directamente a la tabla, configuramos el filtro
        configurarBusqueda();
    }

    @FXML
    private void abrirFormularioProducto(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(App.class.getResource("/cl/fersal/inventario/fxml/producto_form.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Nuevo Producto");
            stage.setScene(new javafx.scene.Scene(root));

            // Esto hace que la ventana del formulario bloquee el dashboard hasta que se cierre
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.showAndWait(); // Pausa la ejecución aquí hasta que la ventana se cierre

            // Refrescar la tabla cuando el usuario termine de agregar el producto
            cargarDatos();

        } catch (java.io.IOException e) {
            System.err.println("Error al abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        try {
            App.cambiarEscena("/cl/fersal/inventario/fxml/login.fxml", "Fersal Inventario - Acceso", 350, 300);
        } catch (IOException e) {
            System.err.println("Error al cargar la pantalla de login: " + e.getMessage());
        }
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar inventario como Excel");
        selector.setInitialFileName("inventario.xlsx");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx")
        );

        Window ventana = tablaProductos.getScene().getWindow();
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }

        Path destino = asegurarExtension(Path.of(archivo.toURI()), ".xlsx");
        exportadorService.exportarExcelAsync(
                destino,
                ruta -> mostrarAlerta(
                        "Exportación completada",
                        "El archivo Excel fue creado en:\n" + ruta
                ),
                error -> mostrarAlerta(
                        "Error de exportación",
                        "No se pudo generar el archivo Excel:\n" + mensajeError(error)
                )
        );
    }

    @FXML
    private void exportarCSV(ActionEvent event) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar inventario como CSV");
        selector.setInitialFileName("inventario.csv");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
        );

        Window ventana = tablaProductos.getScene().getWindow();
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }

        Path destino = asegurarExtension(Path.of(archivo.toURI()), ".csv");
        exportadorService.exportarCSVAsync(
                destino,
                ruta -> mostrarAlerta(
                        "Exportación completada",
                        "El archivo CSV fue creado en:\n" + ruta
                ),
                error -> mostrarAlerta(
                        "Error de exportación",
                        "No se pudo generar el archivo CSV:\n" + mensajeError(error)
                )
        );
    }

    private Path asegurarExtension(Path ruta, String extension) {
        String nombre = ruta.getFileName().toString();
        if (nombre.toLowerCase().endsWith(extension)) {
            return ruta;
        }
        return ruta.resolveSibling(nombre + extension);
    }

    private String mensajeError(Exception error) {
        return error.getMessage() == null
                ? error.getClass().getSimpleName()
                : error.getMessage();
    }

    private void configurarBusqueda() {
        // Envolvemos nuestra lista observable en una FilteredList (por defecto muestra todo)
        FilteredList<Producto> datosFiltrados = new FilteredList<>(listaProductos, p -> true);

        // Escuchamos los cambios en el TextField de búsqueda
        txtBuscar.textProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            datosFiltrados.setPredicate(producto -> {
                // Si el campo de búsqueda está vacío, mostramos todos los productos
                if (valorNuevo == null || valorNuevo.trim().isEmpty()) {
                    return true;
                }

                String busqueda = valorNuevo.toLowerCase();

                // Buscamos coincidencia en el nombre del producto
                if (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(busqueda)) {
                    return true;
                }
                // Buscamos coincidencia en el código interno
                if (producto.getCodigoInterno() != null && producto.getCodigoInterno().toLowerCase().contains(busqueda)) {
                    return true;
                }

                return false; // Si no coincide con nada, se oculta de la tabla
            });
        });

        // Envolvemos los datos filtrados en una SortedList para no perder la función de ordenar columnas
        SortedList<Producto> datosOrdenados = new SortedList<>(datosFiltrados);

        // Enlazamos el ordenamiento de nuestra lista con el de la tabla visual
        datosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());

        // Finalmente, asignamos esta lista procesada a la tabla
        tablaProductos.setItems(datosOrdenados);
    }

    @FXML
    private void eliminarSeleccionado(ActionEvent event) {
        // 1. Obtener el producto que el usuario seleccionó en la tabla
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Advertencia", "Debe seleccionar un producto de la tabla para eliminarlo.");
            return;
        }

        // 2. Pedir confirmación por seguridad
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Eliminación");
        alerta.setHeaderText("¿Está seguro de eliminar: " + seleccionado.getNombre() + "?");
        alerta.setContentText("Esta acción no se puede deshacer.");

        // 3. Ejecutar la eliminación si presiona OK
        if (alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (productoDAO.eliminar(seleccionado.getId())) {
                listaProductos.remove(seleccionado); // Lo quitamos visualmente de la tabla sin consultar la BD de nuevo
            } else {
                mostrarAlerta("Error", "Ocurrió un problema al intentar eliminar el producto de la base de datos.");
            }
        }
    }

    @FXML
    private void editarSeleccionado(ActionEvent event) {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Advertencia", "Debe seleccionar un producto de la tabla para editarlo.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("/cl/fersal/inventario/fxml/producto_form.fxml"));
            javafx.scene.Parent root = loader.load();

            // Magia aquí: Obtenemos el controlador del formulario y le inyectamos el producto seleccionado
            ProductoFormController controller = loader.getController();
            controller.cargarDatosProducto(seleccionado);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Editar Producto - " + seleccionado.getNombre());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            cargarDatos(); // Recargar la tabla por si el usuario modificó algo

        } catch (java.io.IOException e) {
            System.err.println("Error al abrir el formulario de edición: " + e.getMessage());
        }
    }

    // Método de utilidad para no repetir código de alertas
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}