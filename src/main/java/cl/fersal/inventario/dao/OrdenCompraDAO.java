package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.DetalleOrdenCompra;
import cl.fersal.inventario.model.OrdenCompra;
import cl.fersal.inventario.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class OrdenCompraDAO {

    public List<OrdenCompra> obtenerOrdenes() {
        String sql = """
                SELECT o.id, o.fecha, o.proveedor_id,
                       p.nombre AS nombre_proveedor,
                       o.numero_documento, o.neto, o.iva, o.total
                FROM ordenes_compra o
                INNER JOIN proveedores p ON p.id = o.proveedor_id
                ORDER BY o.fecha DESC, o.id DESC
                """;
        List<OrdenCompra> ordenes = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                OrdenCompra orden = new OrdenCompra();
                orden.setId(rs.getInt("id"));
                orden.setProveedorId(rs.getInt("proveedor_id"));
                orden.setFechaTexto(rs.getString("fecha"));
                orden.setNombreProveedor(
                        rs.getString("nombre_proveedor"));
                orden.setNumeroDocumento(
                        rs.getString("numero_documento"));
                orden.setNeto(rs.getDouble("neto"));
                orden.setIva(rs.getDouble("iva"));
                orden.setTotal(rs.getDouble("total"));
                ordenes.add(orden);
            }
            return ordenes;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar las órdenes de compra.", e);
        }
    }

    public List<DetalleOrdenCompra> obtenerDetalle(int ordenId) {
        String sql = """
                SELECT d.id, d.orden_id, d.producto_id, d.cantidad,
                       d.precio_unitario, p.nombre AS nombre_producto
                FROM detalle_orden_compra d
                INNER JOIN productos p ON p.id = d.producto_id
                WHERE d.orden_id = ?
                ORDER BY d.id
                """;
        List<DetalleOrdenCompra> detalles = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ordenId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetalleOrdenCompra detalle =
                            new DetalleOrdenCompra();
                    detalle.setId(rs.getInt("id"));
                    detalle.setOrdenCompraId(rs.getInt("orden_id"));
                    detalle.setProductoId(rs.getInt("producto_id"));
                    detalle.setCantidad(rs.getDouble("cantidad"));
                    detalle.setPrecio(
                            rs.getDouble("precio_unitario"));
                    detalle.setNombreProducto(
                            rs.getString("nombre_producto"));
                    detalles.add(detalle);
                }
            }
            return detalles;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo cargar el detalle de la compra.", e);
        }
    }

    public int registrar(
            Proveedor proveedor,
            String numeroDocumento,
            double neto,
            double iva,
            double total,
            List<DetalleOrdenCompra> detalles) {
        validarEntrada(
                proveedor,
                numeroDocumento,
                neto,
                iva,
                total,
                detalles
        );

        String sqlOrden = """
                INSERT INTO ordenes_compra
                    (proveedor_id, numero_documento, neto, iva, total)
                VALUES (?, ?, ?, ?, ?)
                """;
        String sqlProducto = """
                SELECT nombre, stock_actual
                FROM productos
                WHERE id = ?
                """;
        String sqlDetalle = """
                INSERT INTO detalle_orden_compra
                    (orden_id, producto_id, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """;
        String sqlActualizarProducto = """
                UPDATE productos
                SET stock_actual = ?
                WHERE id = ?
                """;
        String sqlMovimiento = """
                INSERT INTO movimientos_inventario
                    (producto_id, cantidad_afectada, tipo_movimiento,
                     costo_unitario, responsable)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn) {
            conn.setAutoCommit(false);
            int ordenId;

            try (PreparedStatement pstmt = conn.prepareStatement(
                    sqlOrden,
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, proveedor.getId());
                pstmt.setString(2, numeroDocumento.trim());
                pstmt.setDouble(3, neto);
                pstmt.setDouble(4, iva);
                pstmt.setDouble(5, total);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se pudo obtener el ID de la compra.");
                    }
                    ordenId = rs.getInt(1);
                }
            }

            String responsable = "ORDEN_COMPRA_ID_" + ordenId;

            for (DetalleOrdenCompra detalle : detalles) {
                double stockAnterior;
                String nombreProducto;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        sqlProducto)) {
                    pstmt.setInt(1, detalle.getProductoId());

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "El producto no existe: "
                                            + detalle.getNombreProducto());
                        }

                        nombreProducto = rs.getString("nombre");
                        stockAnterior = rs.getDouble("stock_actual");
                    }
                }

                double cantidadIngresada = detalle.getCantidad();
                double precioUnitario = detalle.getPrecio();
                double stockNuevo = stockAnterior + cantidadIngresada;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        sqlActualizarProducto)) {
                    pstmt.setDouble(1, stockNuevo);
                    pstmt.setInt(2, detalle.getProductoId());

                    if (pstmt.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar el producto: "
                                        + nombreProducto);
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        sqlDetalle)) {
                    pstmt.setInt(1, ordenId);
                    pstmt.setInt(2, detalle.getProductoId());
                    pstmt.setDouble(3, cantidadIngresada);
                    pstmt.setDouble(4, precioUnitario);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        sqlMovimiento)) {
                    pstmt.setInt(1, detalle.getProductoId());
                    pstmt.setDouble(2, cantidadIngresada);
                    pstmt.setString(3, "Ingreso por Compra");
                    pstmt.setDouble(4, precioUnitario);
                    pstmt.setString(5, responsable);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return ordenId;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            throw new IllegalStateException(
                    "No se pudo registrar la compra. "
                            + "No se aplicaron cambios.",
                    e
            );
        }
    }

    private void validarEntrada(
            Proveedor proveedor,
            String numeroDocumento,
            double neto,
            double iva,
            double total,
            List<DetalleOrdenCompra> detalles) {
        if (proveedor == null || proveedor.getId() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un proveedor.");
        }
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el número de documento.");
        }
        if (!Double.isFinite(neto)
                || !Double.isFinite(iva)
                || !Double.isFinite(total)
                || neto < 0.0
                || iva < 0.0
                || total < 0.0) {
            throw new IllegalArgumentException(
                    "Los totales de la compra no son válidos.");
        }
        if (Math.abs((neto + iva) - total) > 0.01) {
            throw new IllegalArgumentException(
                    "El total debe ser igual al neto más el IVA.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe agregar al menos un producto.");
        }
        for (DetalleOrdenCompra detalle : detalles) {
            if (detalle == null
                    || detalle.getProductoId() == null
                    || detalle.getCantidad() == null
                    || detalle.getPrecio() == null
                    || !Double.isFinite(detalle.getCantidad())
                    || !Double.isFinite(detalle.getPrecio())
                    || detalle.getCantidad() <= 0.0
                    || detalle.getPrecio() < 0.0) {
                throw new IllegalArgumentException(
                        "Las cantidades deben ser mayores que cero y "
                                + "los precios no pueden ser negativos.");
            }
        }
    }
}
