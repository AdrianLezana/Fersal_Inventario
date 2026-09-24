package cl.fersal.inventario.controller;

import cl.fersal.inventario.model.Producto;
import cl.fersal.inventario.service.ProductoService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProductoFormController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextField txtDimensiones;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtStock;
    @FXML private TextField txtCosto;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtPrecioMetro;
    @FXML private TextField txtPrecioTrabajado;
    @FXML private Label lblMensaje;

    private final ProductoService productoService = new ProductoService();
    private Producto productoEdicion = null;

    public void cargarDatosProducto(Producto producto) {
        this.productoEdicion = producto;

        txtCodigo.setText(producto.getCodigoInterno() != null ? producto.getCodigoInterno() : "");
        txtNombre.setText(producto.getNombre());
        cmbUnidad.setValue(producto.getUnidadMedida());
        txtDimensiones.setText(producto.getDimensiones() != null ? producto.getDimensiones() : "");
        txtUbicacion.setText(producto.getUbicacion() != null ? producto.getUbicacion() : "");

        txtStock.setText(producto.getStockActual() != null ? String.valueOf(producto.getStockActual()) : "0.0");
        txtCosto.setText(producto.getCostoPromedio() != null ? String.valueOf(producto.getCostoPromedio()) : "0.0");
        txtPrecio.setText(producto.getPrecioVenta() != null ? String.valueOf(producto.getPrecioVenta()) : "0.0");
        txtPrecioMetro.setText(producto.getPrecioVentaMetro() != null ? String.valueOf(producto.getPrecioVentaMetro()) : "");
        txtPrecioTrabajado.setText(producto.getPrecioTrabajadoMetro() != null ? String.valueOf(producto.getPrecioTrabajadoMetro()) : "");
    }

    @FXML
    private void guardarProducto(ActionEvent event) {
        try {
            boolean esEdicion = (productoEdicion != null);
            Producto productoGuardar = esEdicion ? productoEdicion : new Producto();

            // Textos
            productoGuardar.setCodigoInterno(txtCodigo.getText().trim());
            productoGuardar.setNombre(txtNombre.getText());
            productoGuardar.setUnidadMedida(cmbUnidad.getValue());
            productoGuardar.setDimensiones(txtDimensiones.getText().trim());
            productoGuardar.setUbicacion(txtUbicacion.getText().trim());

            // Números (usamos un método auxiliar para no repetir código)
            productoGuardar.setStockActual(parsearDoubleSeguro(txtStock.getText()));
            productoGuardar.setCostoPromedio(parsearDoubleSeguro(txtCosto.getText()));
            productoGuardar.setPrecioVenta(parsearDoubleSeguro(txtPrecio.getText()));

            // Estos pueden ser nulos si están vacíos
            productoGuardar.setPrecioVentaMetro(txtPrecioMetro.getText().trim().isEmpty() ? null : Double.parseDouble(txtPrecioMetro.getText().trim()));
            productoGuardar.setPrecioTrabajadoMetro(txtPrecioTrabajado.getText().trim().isEmpty() ? null : Double.parseDouble(txtPrecioTrabajado.getText().trim()));

            productoGuardar.setUsuarioId(1);

            if (esEdicion) {
                productoService.actualizarProducto(
                        productoGuardar,
                        "AJUSTE_MANUAL",
                        "USUARIO_ID_1"
                );
            } else {
                productoService.registrarNuevoProducto(productoGuardar);
            }

            cerrarVentana(event);

        } catch (NumberFormatException e) {
            lblMensaje.setText("Por favor, asegúrese de ingresar números válidos en los campos de stock y precios (use punto para decimales).");
        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        } catch (Exception e) {
            lblMensaje.setText("Error interno: " + e.getMessage());
        }
    }

    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // Método auxiliar para manejar cajas de texto vacías
    private Double parsearDoubleSeguro(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) return 0.0;
        return Double.parseDouble(texto);
    }
}