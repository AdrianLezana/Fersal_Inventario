package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.OrdenTrabajoDAO;
import cl.fersal.inventario.dao.ProductoDAO;
import cl.fersal.inventario.dao.TrabajadorDAO;
import cl.fersal.inventario.model.DetalleOrdenTrabajo;
import cl.fersal.inventario.model.Producto;
import cl.fersal.inventario.model.Trabajador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class OrdenTrabajoController {
    @FXML private ComboBox<Trabajador> cmbTrabajador;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtDescripcion;
    @FXML private TableView<DetalleOrdenTrabajo> tablaMateriales;
    @FXML private TableColumn<DetalleOrdenTrabajo, String> colMaterial;
    @FXML private TableColumn<DetalleOrdenTrabajo, Double> colCantidad;
    @FXML private TableColumn<DetalleOrdenTrabajo, String> colUnidad;
    @FXML private Label lblMensaje;

    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final OrdenTrabajoDAO ordenTrabajoDAO = new OrdenTrabajoDAO();
    private final ObservableList<DetalleOrdenTrabajo> materiales =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadUtilizada"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
        tablaMateriales.setItems(materiales);
        cmbTrabajador.setItems(
                FXCollections.observableArrayList(trabajadorDAO.obtenerTodos()));
        cmbProducto.setItems(
                FXCollections.observableArrayList(productoDAO.obtenerTodos()));
    }

    @FXML
    private void agregarMaterial(ActionEvent event) {
        try {
            Producto producto = cmbProducto.getValue();
            if (producto == null) {
                throw new IllegalArgumentException("Seleccione un producto.");
            }
            double cantidad = Double.parseDouble(
                    txtCantidad.getText().trim().replace(',', '.'));
            if (!Double.isFinite(cantidad) || cantidad <= 0.0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser un número mayor que cero.");
            }

            DetalleOrdenTrabajo existente = materiales.stream()
                    .filter(item -> item.getProductoId().equals(producto.getId()))
                    .findFirst()
                    .orElse(null);
            if (existente == null) {
                materiales.add(new DetalleOrdenTrabajo(
                        producto.getId(), producto.getNombre(),
                        producto.getUnidadMedida(), cantidad));
            } else {
                existente.setCantidadUtilizada(
                        existente.getCantidadUtilizada() + cantidad);
                tablaMateriales.refresh();
            }
            txtCantidad.clear();
            cmbProducto.getSelectionModel().clearSelection();
            lblMensaje.setText("");
        } catch (NumberFormatException e) {
            lblMensaje.setText("Ingrese una cantidad numérica válida.");
        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void quitarMaterial(ActionEvent event) {
        DetalleOrdenTrabajo seleccionado =
                tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblMensaje.setText("Seleccione un material para quitarlo.");
            return;
        }
        materiales.remove(seleccionado);
    }

    @FXML
    private void confirmarOrden(ActionEvent event) {
        try {
            int ordenId = ordenTrabajoDAO.registrar(
                    cmbTrabajador.getValue(),
                    txtDescripcion.getText(),
                    List.copyOf(materiales));
            mostrarAlerta(
                    "Orden registrada",
                    "La Orden de Trabajo #" + ordenId
                            + " fue registrada y los materiales fueron descontados.");
            cerrarVentana(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
