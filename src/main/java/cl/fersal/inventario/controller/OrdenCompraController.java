package cl.fersal.inventario.controller;

import cl.fersal.inventario.app.App;
import cl.fersal.inventario.dao.OrdenCompraDAO;
import cl.fersal.inventario.dao.ProductoDAO;
import cl.fersal.inventario.dao.ProveedorDAO;
import cl.fersal.inventario.model.DetalleOrdenCompra;
import cl.fersal.inventario.model.Producto;
import cl.fersal.inventario.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class OrdenCompraController {
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextField txtNumeroDocumento;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioUnitario;
    @FXML private TableView<DetalleOrdenCompra> tablaDetalles;
    @FXML private TableColumn<DetalleOrdenCompra, String> colProducto;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colCantidad;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colPrecio;
    @FXML private TableColumn<DetalleOrdenCompra, Double> colSubtotal;
    @FXML private Label lblNeto;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final OrdenCompraDAO ordenCompraDAO = new OrdenCompraDAO();
    private final ObservableList<DetalleOrdenCompra> detalles =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(
                new PropertyValueFactory<>("subtotal"));
        tablaDetalles.setItems(detalles);

        cmbProveedor.setItems(
                FXCollections.observableArrayList(
                        proveedorDAO.obtenerTodos()));
        cmbProducto.setItems(
                FXCollections.observableArrayList(
                        productoDAO.obtenerTodos()));
        actualizarTotales();
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        try {
            Producto producto = cmbProducto.getValue();
            if (producto == null) {
                throw new IllegalArgumentException(
                        "Seleccione un producto.");
            }

            double cantidad = parsearNumero(txtCantidad.getText());
            double precio = parsearNumero(txtPrecioUnitario.getText());

            if (!Double.isFinite(cantidad) || cantidad <= 0.0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero.");
            }
            if (!Double.isFinite(precio) || precio < 0.0) {
                throw new IllegalArgumentException(
                        "El precio no puede ser negativo.");
            }

            DetalleOrdenCompra existente = detalles.stream()
                    .filter(item -> item.getProductoId()
                            .equals(producto.getId()))
                    .findFirst()
                    .orElse(null);

            if (existente == null) {
                DetalleOrdenCompra detalle = new DetalleOrdenCompra();
                detalle.setProductoId(producto.getId());
                detalle.setCantidad(cantidad);
                detalle.setPrecio(precio);
                detalle.setNombreProducto(producto.getNombre());
                detalle.setUnidadMedida(producto.getUnidadMedida());
                detalles.add(detalle);
            } else {
                existente.setCantidad(
                        existente.getCantidad() + cantidad);
                existente.setPrecio(precio);
                tablaDetalles.refresh();
            }

            limpiarEntradaProducto();
            actualizarTotales();
            lblMensaje.setText("");
        } catch (NumberFormatException e) {
            lblMensaje.setText(
                    "Ingrese cantidades y precios numéricos válidos.");
        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void nuevoProducto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource(
                            "/cl/fersal/inventario/fxml/producto_form.fxml"
                    )
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Producto");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initOwner(cmbProducto.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            cmbProducto.setItems(
                    FXCollections.observableArrayList(
                            productoDAO.obtenerTodos()
                    )
            );
            cmbProducto.getSelectionModel().clearSelection();
        } catch (IOException e) {
            mostrarError(
                    "No se pudo abrir el formulario de producto.",
                    e
            );
        } catch (IllegalStateException e) {
            mostrarError(
                    "No se pudo actualizar la lista de productos.",
                    e
            );
        }
    }

    @FXML
    private void quitarProducto(ActionEvent event) {
        DetalleOrdenCompra seleccionado =
                tablaDetalles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblMensaje.setText(
                    "Seleccione un producto para quitarlo.");
            return;
        }

        detalles.remove(seleccionado);
        actualizarTotales();
    }

    @FXML
    private void confirmarCompra(ActionEvent event) {
        try {
            double neto = calcularNeto();
            double iva = redondear(neto * 0.19);
            double total = redondear(neto + iva);

            int ordenId = ordenCompraDAO.registrar(
                    cmbProveedor.getValue(),
                    txtNumeroDocumento.getText(),
                    neto,
                    iva,
                    total,
                    List.copyOf(detalles)
            );

            mostrarAlerta(
                    "Compra registrada",
                    "La Orden de Compra #" + ordenId
                            + " fue registrada y el stock fue actualizado."
            );
            cerrarVentana(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        stage.close();
    }

    private void actualizarTotales() {
        double neto = calcularNeto();
        double iva = redondear(neto * 0.19);
        double total = redondear(neto + iva);

        lblNeto.setText(formatear(neto));
        lblIva.setText(formatear(iva));
        lblTotal.setText(formatear(total));
    }

    private double calcularNeto() {
        return detalles.stream()
                .mapToDouble(detalle ->
                        detalle.getCantidad() * detalle.getPrecio())
                .sum();
    }

    private double parsearNumero(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new NumberFormatException();
        }
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String formatear(double valor) {
        return String.format("%.2f", valor);
    }

    private void limpiarEntradaProducto() {
        cmbProducto.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtPrecioUnitario.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String encabezado, Exception error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(encabezado);
        alert.setContentText(error.getMessage());
        alert.showAndWait();
    }
}
