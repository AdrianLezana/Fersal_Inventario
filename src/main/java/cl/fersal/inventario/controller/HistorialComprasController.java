package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.OrdenCompraDAO;
import cl.fersal.inventario.model.DetalleOrdenCompra;
import cl.fersal.inventario.model.OrdenCompra;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Locale;
import java.util.List;

public class HistorialComprasController {
    @FXML private TextField txtBuscar;
    @FXML private TableView<OrdenCompra> tablaOrdenes;
    @FXML private TableColumn<OrdenCompra, Integer> colId;
    @FXML private TableColumn<OrdenCompra, String> colFecha;
    @FXML private TableColumn<OrdenCompra, String> colProveedor;
    @FXML private TableColumn<OrdenCompra, String> colDocumento;
    @FXML private TableColumn<OrdenCompra, Double> colNeto;
    @FXML private TableColumn<OrdenCompra, Double> colIva;
    @FXML private TableColumn<OrdenCompra, Double> colTotal;
    @FXML private TableColumn<OrdenCompra, String> colEstado;

    @FXML private TableView<DetalleOrdenCompra> tablaDetalle;
    @FXML private TableColumn<DetalleOrdenCompra, String> colProducto;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colCantidad;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colPrecioUnitario;

    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO();
    private final ObservableList<OrdenCompra> ordenes =
            FXCollections.observableArrayList();
    private FilteredList<OrdenCompra> ordenesFiltradas;

    @FXML
    public void initialize() {
        tablaOrdenes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaTexto"));
        colProveedor.setCellValueFactory(
                new PropertyValueFactory<>("nombreProveedor"));
        colDocumento.setCellValueFactory(
                new PropertyValueFactory<>("numeroDocumento"));
        colNeto.setCellValueFactory(
                new PropertyValueFactory<>("neto"));
        colIva.setCellValueFactory(
                new PropertyValueFactory<>("iva"));
        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado"));

        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(
                new PropertyValueFactory<>("precio"));

        ordenesFiltradas = new FilteredList<>(ordenes, orden -> true);
        SortedList<OrdenCompra> ordenesOrdenadas =
                new SortedList<>(ordenesFiltradas);
        ordenesOrdenadas.comparatorProperty().bind(
                tablaOrdenes.comparatorProperty());
        tablaOrdenes.setItems(ordenesOrdenadas);
        txtBuscar.textProperty().addListener(
                (observable, anterior, texto) ->
                        filtrarOrdenes(texto));

        tablaOrdenes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) ->
                        cargarDetalle(seleccionada));

        cargarOrdenes();
    }

    public void cargarOrdenes() {
        try {
            ordenes.setAll(ordenCompraDAO.obtenerOrdenes());
            tablaDetalle.setItems(
                    FXCollections.observableArrayList());
        } catch (IllegalStateException e) {
            mostrarError(
                    "No se pudo cargar el historial de compras.",
                    e);
        }
    }

    private void filtrarOrdenes(String texto) {
        String consulta = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);
        ordenesFiltradas.setPredicate(orden -> consulta.isEmpty()
                || contiene(String.valueOf(orden.getId()), consulta)
                || contiene(orden.getNumeroDocumento(), consulta)
                || contiene(orden.getNombreProveedor(), consulta)
                || contiene(orden.getRutProveedor(), consulta));
    }

    private boolean contiene(String valor, String consulta) {
        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(consulta);
    }

    private void cargarDetalle(OrdenCompra orden) {
        if (orden == null || orden.getId() == null) {
            tablaDetalle.setItems(
                    FXCollections.observableArrayList());
            return;
        }

        try {
            List<DetalleOrdenCompra> detalle =
                    ordenCompraDAO.obtenerDetalle(orden.getId());
            tablaDetalle.setItems(
                    FXCollections.observableArrayList(detalle));
        } catch (IllegalStateException e) {
            mostrarError(
                    "No se pudo cargar el detalle de la compra.",
                    e);
        }
    }

    @FXML
    private void anularOrdenSeleccionada() {
        OrdenCompra seleccionada =
                tablaOrdenes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso(
                    "Seleccione una factura para anular.");
            return;
        }

        if ("ANULADA".equalsIgnoreCase(seleccionada.getEstado())) {
            mostrarAviso(
                    "La factura seleccionada ya está anulada.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar anulación");
        confirmacion.setHeaderText(
                "¿Anular la Factura #"
                        + seleccionada.getId() + "?");
        confirmacion.setContentText(
                "Se descontarán del stock las cantidades ingresadas "
                        + "y se registrará el reverso en el Kardex. "
                        + "La operación no continuará si el stock "
                        + "disponible es insuficiente.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        Integer idOrden = seleccionada.getId();
        try {
            ordenCompraDAO.anularOrden(idOrden);
            cargarOrdenes();
            tablaOrdenes.getItems().stream()
                    .filter(orden -> orden.getId().equals(idOrden))
                    .findFirst()
                    .ifPresent(orden ->
                            tablaOrdenes.getSelectionModel().select(orden));
        } catch (IllegalStateException e) {
            mostrarError(
                    "No se pudo anular la factura.",
                    e);
        }
    }

    private void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Anulación de compra");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String encabezado, Exception error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(error.getMessage());
        alerta.showAndWait();
    }
}
