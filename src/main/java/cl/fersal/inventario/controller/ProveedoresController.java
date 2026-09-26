package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.ProveedorDAO;
import cl.fersal.inventario.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ProveedoresController {
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colRut;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TextField txtRut;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private Label lblMensaje;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ObservableList<Proveedor> proveedores =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colRut.setCellValueFactory(new PropertyValueFactory<>("rut"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(
                new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tablaProveedores.setItems(proveedores);
        cargarProveedores();
    }

    @FXML
    private void agregarProveedor(ActionEvent event) {
        try {
            Proveedor proveedor = new Proveedor(
                    null,
                    txtNombre.getText(),
                    txtRut.getText(),
                    txtTelefono.getText(),
                    txtEmail.getText(),
                    null
            );

            proveedorDAO.insertar(proveedor);
            proveedores.add(proveedor);
            limpiarFormulario();
            mostrarMensaje("");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensaje(e.getMessage());
        }
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
            mostrarMensaje("");
        } catch (IllegalStateException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtRut.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtRut.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje == null ? "" : mensaje);
    }
}
