package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.KardexDAO;
import cl.fersal.inventario.model.MovimientoInventario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class KardexController {

    @FXML private TableView<MovimientoInventario> tablaKardex;

    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colProducto;
    @FXML private TableColumn<MovimientoInventario, Double> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colTipoMovimiento;
    @FXML private TableColumn<MovimientoInventario, Double> colCostoUnitario;
    @FXML private TableColumn<MovimientoInventario, String> colResponsable;

    private final KardexDAO kardexDAO = new KardexDAO();

    @FXML
    public void initialize() {

        // Asegurar que las columnas se ajusten al ancho de la ventana automáticamente
        tablaKardex.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha")
        );

        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto")
        );

        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidadAfectada")
        );

        colTipoMovimiento.setCellValueFactory(
                new PropertyValueFactory<>("tipoMovimiento")
        );

        colCostoUnitario.setCellValueFactory(
                new PropertyValueFactory<>("costoUnitario")
        );

        colResponsable.setCellValueFactory(
                new PropertyValueFactory<>("responsable")
        );

        cargarMovimientos();
    }

    private void cargarMovimientos() {
        try {
            List<MovimientoInventario> movimientos =
                    kardexDAO.obtenerTodos();

            tablaKardex.setItems(
                    FXCollections.observableArrayList(movimientos)
            );

        } catch (IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText("No se pudo cargar el Kardex");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }
}