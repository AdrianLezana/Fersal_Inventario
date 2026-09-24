package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.TrabajadorDAO;
import cl.fersal.inventario.model.Trabajador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TrabajadoresController {
    @FXML private TableView<Trabajador> tablaTrabajadores;
    @FXML private TableColumn<Trabajador, String> colRut;
    @FXML private TableColumn<Trabajador, String> colNombre;
    @FXML private TableColumn<Trabajador, String> colCargo;
    @FXML private TextField txtRut;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCargo;
    @FXML private Label lblMensaje;

    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final ObservableList<Trabajador> trabajadores =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colRut.setCellValueFactory(new PropertyValueFactory<>("rut"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        tablaTrabajadores.setItems(trabajadores);
        cargarTrabajadores();
    }

    @FXML
    private void agregarTrabajador(ActionEvent event) {
        try {
            Trabajador trabajador = new Trabajador(
                    null,
                    txtRut.getText(),
                    txtNombre.getText(),
                    txtCargo.getText()
            );

            trabajadorDAO.insertar(trabajador);
            trabajadores.add(trabajador);
            limpiarFormulario();
            mostrarMensaje("");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    @FXML
    private void eliminarTrabajador(ActionEvent event) {
        Trabajador seleccionado =
                tablaTrabajadores.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccione un trabajador para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + seleccionado.getNombre() + "?",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            if (trabajadorDAO.eliminar(seleccionado.getId())) {
                trabajadores.remove(seleccionado);
                mostrarMensaje("");
            } else {
                mostrarMensaje("No se encontró el trabajador seleccionado.");
            }
        } catch (IllegalStateException e) {
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

    private void cargarTrabajadores() {
        try {
            trabajadores.setAll(trabajadorDAO.obtenerTodos());
            mostrarMensaje("");
        } catch (IllegalStateException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtRut.clear();
        txtNombre.clear();
        txtCargo.clear();
        txtRut.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje == null ? "" : mensaje);
    }
}
