package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.OrdenTrabajoDAO;
import cl.fersal.inventario.model.DetalleOrdenTrabajo;
import cl.fersal.inventario.model.OrdenTrabajo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HistorialOrdenesController {
    @FXML private TableView<OrdenTrabajo> tablaOrdenes;
    @FXML private TableColumn<OrdenTrabajo, Integer> colId;
    @FXML private TableColumn<OrdenTrabajo, String> colFecha;
    @FXML private TableColumn<OrdenTrabajo, String> colTrabajador;
    @FXML private TableColumn<OrdenTrabajo, String> colDescripcion;

    @FXML private TableView<DetalleOrdenTrabajo> tablaDetalles;
    @FXML private TableColumn<DetalleOrdenTrabajo, String> colProducto;
    @FXML private TableColumn<DetalleOrdenTrabajo, Double> colCantidad;

    private final OrdenTrabajoDAO ordenTrabajoDAO = new OrdenTrabajoDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaTexto"));
        colTrabajador.setCellValueFactory(
                new PropertyValueFactory<>("nombreTrabajador"));
        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcionTrabajo"));

        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidadUtilizada"));

        tablaOrdenes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) ->
                        cargarDetalles(seleccionada));

        cargarOrdenes();
    }

    private void cargarOrdenes() {
        try {
            List<OrdenTrabajo> ordenes = ordenTrabajoDAO.obtenerTodas();
            tablaOrdenes.setItems(
                    FXCollections.observableArrayList(ordenes));
            tablaDetalles.setItems(
                    FXCollections.observableArrayList());
        } catch (IllegalStateException e) {
            mostrarError("No se pudo cargar el historial de órdenes.", e);
        }
    }

    private void cargarDetalles(OrdenTrabajo orden) {
        if (orden == null || orden.getId() == null) {
            tablaDetalles.setItems(
                    FXCollections.observableArrayList());
            return;
        }

        try {
            List<DetalleOrdenTrabajo> detalles =
                    ordenTrabajoDAO.obtenerDetalles(orden.getId());
            tablaDetalles.setItems(
                    FXCollections.observableArrayList(detalles));
        } catch (IllegalStateException e) {
            mostrarError("No se pudo cargar el detalle de la orden.", e);
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
