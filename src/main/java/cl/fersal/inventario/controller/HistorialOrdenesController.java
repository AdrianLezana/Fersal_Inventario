package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.OrdenTrabajoDAO;
import cl.fersal.inventario.model.DetalleOrdenTrabajo;
import cl.fersal.inventario.model.OrdenTrabajo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
    @FXML private TableColumn<OrdenTrabajo, String> colEstado;

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
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado"));

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

    public void cargarOrdenes() {
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

    @FXML
    private void anularOrdenSeleccionada() {
        OrdenTrabajo seleccionada =
                tablaOrdenes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Sin selección");
            alerta.setHeaderText(null);
            alerta.setContentText("Seleccione una orden para anular.");
            alerta.showAndWait();
            return;
        }

        if ("ANULADA".equalsIgnoreCase(seleccionada.getEstado())) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Orden ya anulada");
            alerta.setHeaderText(null);
            alerta.setContentText(
                    "La orden seleccionada ya fue anulada.");
            alerta.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar anulación");
        confirmacion.setHeaderText(
                "¿Anular la Orden de Trabajo #"
                        + seleccionada.getId() + "?");
        confirmacion.setContentText(
                "Se devolverán sus materiales al inventario y quedará "
                        + "registrado el reverso en el Kardex.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        Integer idOrden = seleccionada.getId();
        try {
            ordenTrabajoDAO.anularOrden(idOrden);
            cargarOrdenes();
            tablaOrdenes.getItems().stream()
                    .filter(orden -> orden.getId().equals(idOrden))
                    .findFirst()
                    .ifPresent(orden ->
                            tablaOrdenes.getSelectionModel().select(orden));

            Alert resultado = new Alert(Alert.AlertType.INFORMATION);
            resultado.setTitle("Orden anulada");
            resultado.setHeaderText(null);
            resultado.setContentText(
                    "La orden fue anulada y sus materiales fueron "
                            + "devueltos al inventario.");
            resultado.showAndWait();
        } catch (IllegalStateException e) {
            mostrarError("No se pudo anular la orden.", e);
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
