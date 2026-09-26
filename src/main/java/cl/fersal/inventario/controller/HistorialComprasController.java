package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.OrdenCompraDAO;
import cl.fersal.inventario.model.DetalleOrdenCompra;
import cl.fersal.inventario.model.OrdenCompra;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HistorialComprasController {
    @FXML private TableView<OrdenCompra> tablaOrdenes;
    @FXML private TableColumn<OrdenCompra, Integer> colId;
    @FXML private TableColumn<OrdenCompra, String> colFecha;
    @FXML private TableColumn<OrdenCompra, String> colProveedor;
    @FXML private TableColumn<OrdenCompra, String> colDocumento;
    @FXML private TableColumn<OrdenCompra, Double> colNeto;
    @FXML private TableColumn<OrdenCompra, Double> colIva;
    @FXML private TableColumn<OrdenCompra, Double> colTotal;

    @FXML private TableView<DetalleOrdenCompra> tablaDetalle;
    @FXML private TableColumn<DetalleOrdenCompra, String> colProducto;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colCantidad;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colPrecioUnitario;

    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO();

    @FXML
    public void initialize() {
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

        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(
                new PropertyValueFactory<>("precio"));

        tablaOrdenes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) ->
                        cargarDetalle(seleccionada));

        cargarOrdenes();
    }

    private void cargarOrdenes() {
        try {
            tablaOrdenes.setItems(
                    FXCollections.observableArrayList(
                            ordenCompraDAO.obtenerOrdenes()));
            tablaDetalle.setItems(
                    FXCollections.observableArrayList());
        } catch (IllegalStateException e) {
            mostrarError(
                    "No se pudo cargar el historial de compras.",
                    e);
        }
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

    private void mostrarError(String encabezado, Exception error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(error.getMessage());
        alerta.showAndWait();
    }
}
