package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /**
     * CREATE: Inserta un nuevo producto y retorna el objeto con su ID asignado.
     */
    public Producto crear(Producto producto) {
        String sqlProducto = "INSERT INTO productos (codigo_interno, categoria_id, nombre, dimensiones, " +
                "unidad_medida, ubicacion, stock_actual, precio_venta, precio_venta_metro, " +
                "precio_trabajado_metro, costo_promedio, usuario_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlMovimiento = "INSERT INTO movimientos_inventario " +
                "(producto_id, cantidad_afectada, tipo_movimiento, costo_unitario, responsable) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            return null;
        }

        try (conn) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                asignarParametros(pstmt, producto);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        producto.setId(rs.getInt(1));
                    }
                }
            }

            if (producto.getStockActual() != null && producto.getStockActual() != 0.0) {
                registrarMovimiento(
                        conn,
                        sqlMovimiento,
                        producto.getId(),
                        producto.getStockActual(),
                        "INGRESO_INICIAL",
                        producto.getCostoPromedio(),
                        responsable(producto.getUsuarioId())
                );
            }

            conn.commit();
            return producto;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            System.err.println("Error al crear producto: " + e.getMessage());
            return null;
        }
    }

    /**
     * READ: Obtiene todo el inventario.
     */
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            throw new IllegalStateException("No se pudo obtener el inventario para exportar.", e);
        }
        return productos;
    }

    /**
     * UPDATE: Actualiza los datos de un producto existente.
     */
    public boolean actualizar(Producto producto) {
        return actualizar(producto, "AJUSTE_MANUAL", responsable(producto.getUsuarioId()));
    }

    /**
     * Actualiza un producto y registra cualquier diferencia de stock en la misma transacción.
     */
    public boolean actualizar(
            Producto producto,
            String tipoMovimiento,
            String responsable) {
        String sqlProducto = "UPDATE productos SET codigo_interno = ?, categoria_id = ?, nombre = ?, " +
                "dimensiones = ?, unidad_medida = ?, ubicacion = ?, stock_actual = ?, " +
                "precio_venta = ?, precio_venta_metro = ?, precio_trabajado_metro = ?, " +
                "costo_promedio = ?, usuario_id = ? WHERE id = ?";
        String sqlStockAnterior = "SELECT stock_actual FROM productos WHERE id = ?";
        String sqlMovimiento = "INSERT INTO movimientos_inventario " +
                "(producto_id, cantidad_afectada, tipo_movimiento, costo_unitario, responsable) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            return false;
        }

        try (conn) {
            conn.setAutoCommit(false);
            double stockAnterior;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStockAnterior)) {
                pstmt.setInt(1, producto.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    stockAnterior = rs.getDouble("stock_actual");
                }
            }

            int filasAfectadas;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlProducto)) {
                asignarParametros(pstmt, producto);
                pstmt.setInt(13, producto.getId());
                filasAfectadas = pstmt.executeUpdate();
            }

            if (filasAfectadas == 0) {
                conn.rollback();
                return false;
            }

            double stockNuevo = producto.getStockActual() != null
                    ? producto.getStockActual()
                    : 0.0;
            double cantidadAfectada = stockNuevo - stockAnterior;

            if (Double.compare(cantidadAfectada, 0.0) != 0) {
                registrarMovimiento(
                        conn,
                        sqlMovimiento,
                        producto.getId(),
                        cantidadAfectada,
                        tipoMovimiento,
                        producto.getCostoPromedio(),
                        responsable
                );
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    private void registrarMovimiento(
            Connection conn,
            String sql,
            int productoId,
            double cantidadAfectada,
            String tipoMovimiento,
            Double costoUnitario,
            String responsable) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productoId);
            pstmt.setDouble(2, cantidadAfectada);
            pstmt.setString(3, tipoMovimiento);
            pstmt.setDouble(4, costoUnitario != null ? costoUnitario : 0.0);
            pstmt.setString(5, responsable);
            pstmt.executeUpdate();
        }
    }

    private String responsable(Integer usuarioId) {
        return usuarioId == null ? "SISTEMA" : "USUARIO_ID_" + usuarioId;
    }

    /**
     * DELETE: Elimina un producto por su ID.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // Métodos Auxiliares
    // ==========================================

    /**
     * Centraliza la asignación de parámetros para evitar repetir código en Crear y Actualizar.
     * Utiliza setObject() para permitir valores nulos en columnas opcionales.
     */
    private void asignarParametros(PreparedStatement pstmt, Producto producto) throws SQLException {
        pstmt.setObject(1, producto.getCodigoInterno(), Types.VARCHAR);
        pstmt.setObject(2, producto.getCategoriaId(), Types.INTEGER); // Llave foránea (puede ser null inicialmente)
        pstmt.setString(3, producto.getNombre());
        pstmt.setObject(4, producto.getDimensiones(), Types.VARCHAR);
        pstmt.setString(5, producto.getUnidadMedida());
        pstmt.setObject(6, producto.getUbicacion(), Types.VARCHAR);
        pstmt.setDouble(7, producto.getStockActual() != null ? producto.getStockActual() : 0.0);
        pstmt.setDouble(8, producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0.0);
        pstmt.setObject(9, producto.getPrecioVentaMetro(), Types.REAL);
        pstmt.setObject(10, producto.getPrecioTrabajadoMetro(), Types.REAL);
        pstmt.setDouble(11, producto.getCostoPromedio() != null ? producto.getCostoPromedio() : 0.0);
        pstmt.setInt(12, producto.getUsuarioId()); // Llave foránea (obligatoria para trazabilidad)
    }

    /**
     * Convierte una fila del ResultSet de SQLite a un objeto Producto de Java.
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigoInterno(rs.getString("codigo_interno"));

        // Manejo seguro de llaves foráneas que podrían ser nulas
        int categoriaId = rs.getInt("categoria_id");
        if (!rs.wasNull()) p.setCategoriaId(categoriaId);

        p.setNombre(rs.getString("nombre"));
        p.setDimensiones(rs.getString("dimensiones"));
        p.setUnidadMedida(rs.getString("unidad_medida"));
        p.setUbicacion(rs.getString("ubicacion"));
        p.setStockActual(rs.getDouble("stock_actual"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));

        // Manejo de decimales opcionales
        double precioMetro = rs.getDouble("precio_venta_metro");
        if (!rs.wasNull()) p.setPrecioVentaMetro(precioMetro);

        double precioTrabajado = rs.getDouble("precio_trabajado_metro");
        if (!rs.wasNull()) p.setPrecioTrabajadoMetro(precioTrabajado);

        p.setCostoPromedio(rs.getDouble("costo_promedio"));
        p.setUsuarioId(rs.getInt("usuario_id"));

        return p;
    }

    /**
     * Verifica si ya existe un producto con el nombre exacto (ignorando mayúsculas/minúsculas).
     */
    public boolean existeProductoPorNombre(String nombre) {
        String sql = "SELECT COUNT(id) FROM productos WHERE LOWER(nombre) = LOWER(?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retorna true si encontró 1 o más coincidencias
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre duplicado: " + e.getMessage());
        }
        return false;
    }
}