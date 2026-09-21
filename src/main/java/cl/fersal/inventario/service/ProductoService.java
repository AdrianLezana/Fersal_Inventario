package cl.fersal.inventario.service;

import cl.fersal.inventario.dao.ProductoDAO;
import cl.fersal.inventario.model.Producto;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        // Inicializamos la conexión a la capa de datos
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Valida las reglas de negocio antes de enviar el producto a la base de datos.
     */
    public Producto registrarNuevoProducto(Producto producto) throws IllegalArgumentException {
        // 1. Validaciones de campos obligatorios básicos
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        if (producto.getUnidadMedida() == null || producto.getUnidadMedida().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar una unidad de medida.");
        }
        if (producto.getUsuarioId() == null) {
            throw new IllegalArgumentException("Error de sistema: No se ha identificado al usuario que registra el producto.");
        }

        // 2. Limpieza de datos (Quitar espacios en blanco accidentales al inicio o final)
        String nombreLimpio = producto.getNombre().trim();
        producto.setNombre(nombreLimpio);

        // 3. Validación de regla de negocio: Evitar duplicados
        if (productoDAO.existeProductoPorNombre(nombreLimpio)) {
            throw new IllegalArgumentException("Rechazado: Ya existe un producto registrado con el nombre '" + nombreLimpio + "'.");
        }

        // 4. Si todas las validaciones pasan, se envía al DAO
        Producto productoCreado = productoDAO.crear(producto);

        if (productoCreado == null) {
            throw new RuntimeException("Ocurrió un error interno al intentar guardar en la base de datos.");
        }

        return productoCreado;
    }
}