package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.ProveedorDAO;
import cl.fersal.inventario.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ProveedoresController {
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colRut;
    @FXML private TableColumn<Proveedor, String> colRazonSocial;
    @FXML private TableColumn<Proveedor, String> colGiro;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colDireccion;
    @FXML private TextField txtRut;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtGiro;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ObservableList<Proveedor> proveedores =
            FXCollections.observableArrayList();
    private Proveedor proveedorEnEdicion;

    @FXML
    public void initialize() {
        tablaProveedores.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        colRut.setCellValueFactory(new PropertyValueFactory<>("rut"));
        colRazonSocial.setCellValueFactory(
                new PropertyValueFactory<>("razonSocial"));
        colGiro.setCellValueFactory(new PropertyValueFactory<>("giro"));
        colTelefono.setCellValueFactory(
                new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccion.setCellValueFactory(
                new PropertyValueFactory<>("direccion"));
        tablaProveedores.setItems(proveedores);
        cargarProveedores();
    }

    @FXML
    private void guardar(ActionEvent event) {
        Proveedor proveedor = new Proveedor(
                proveedorEnEdicion == null ? null : proveedorEnEdicion.getId(),
                txtRut.getText(),
                txtRazonSocial.getText(),
                txtGiro.getText(),
                txtTelefono.getText(),
                txtEmail.getText(),
                txtDireccion.getText());
        try {
            if (proveedorEnEdicion == null) {
                proveedorDAO.crear(proveedor);
            } else {
                proveedorDAO.actualizar(proveedor);
            }
            cargarProveedores();
            limpiarFormulario();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError("No se pudo guardar el proveedor", e.getMessage());
        }
    }

    @FXML
    private void editar(ActionEvent event) {
        Proveedor seleccionado =
                tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Sin selección", "Seleccione un proveedor para editar.");
            return;
        }

        proveedorEnEdicion = seleccionado;
        txtRut.setText(seleccionado.getRut());
        txtRazonSocial.setText(seleccionado.getRazonSocial());
        txtGiro.setText(seleccionado.getGiro());
        txtTelefono.setText(seleccionado.getTelefono());
        txtEmail.setText(seleccionado.getEmail());
        txtDireccion.setText(seleccionado.getDireccion());
    }

    @FXML
    private void eliminar(ActionEvent event) {
        Proveedor seleccionado =
                tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Sin selección", "Seleccione un proveedor para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(
                "¿Eliminar al proveedor " + seleccionado.getRazonSocial() + "?");
        confirmacion.setContentText(
                "Esta acción no se puede deshacer.");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            proveedorDAO.eliminar(seleccionado.getId());
            cargarProveedores();
            if (proveedorEnEdicion != null
                    && proveedorEnEdicion.getId().equals(seleccionado.getId())) {
                limpiarFormulario();
            }
        } catch (IllegalStateException e) {
            mostrarError("No se pudo eliminar el proveedor", e.getMessage());
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        limpiarFormulario();
    }

    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        stage.close();
    }

    private void cargarProveedores() {
        try {
            proveedores.setAll(proveedorDAO.obtenerTodos());
        } catch (IllegalStateException e) {
            mostrarError("No se pudieron cargar los proveedores", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        proveedorEnEdicion = null;
        txtRut.clear();
        txtRazonSocial.clear();
        txtGiro.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        tablaProveedores.getSelectionModel().clearSelection();
        txtRut.requestFocus();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
