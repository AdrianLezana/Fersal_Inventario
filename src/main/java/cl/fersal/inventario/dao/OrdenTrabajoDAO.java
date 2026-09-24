package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.DetalleOrdenTrabajo;
import cl.fersal.inventario.model.OrdenTrabajo;
import cl.fersal.inventario.model.Trabajador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class OrdenTrabajoDAO {
    public List<OrdenTrabajo> obtenerTodas() {
        String sql = """
                SELECT o.id, o.fecha, o.trabajador_id,
                       t.nombre AS nombre_trabajador,
                       o.descripcion_trabajo
                FROM ordenes_trabajo o
                INNER JOIN trabajadores t ON t.id = o.trabajador_id
                ORDER BY o.fecha DESC, o.id DESC
                """;
        List<OrdenTrabajo> ordenes = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                OrdenTrabajo orden = new OrdenTrabajo();
                orden.setId(rs.getInt("id"));
                orden.setTrabajadorId(rs.getInt("trabajador_id"));
                orden.setFechaTexto(rs.getString("fecha"));
                orden.setNombreTrabajador(rs.getString("nombre_trabajador"));
                orden.setDescripcionTrabajo(
                        rs.getString("descripcion_trabajo"));
                ordenes.add(orden);
            }
            return ordenes;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar las órdenes de trabajo.", e);
        }
    }

    public List<DetalleOrdenTrabajo> obtenerDetalles(int ordenTrabajoId) {
        String sql = """
                SELECT d.id, d.orden_trabajo_id, d.producto_id,
                       d.cantidad, p.nombre AS nombre_producto,
                       p.unidad_medida
                FROM ordenes_trabajo_detalle d
                INNER JOIN productos p ON p.id = d.producto_id
                WHERE d.orden_trabajo_id = ?
                ORDER BY d.id
                """;
        List<DetalleOrdenTrabajo> detalles = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ordenTrabajoId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetalleOrdenTrabajo detalle = new DetalleOrdenTrabajo(
                            rs.getInt("id"),
                            rs.getInt("orden_trabajo_id"),
                            rs.getInt("producto_id"),
                            rs.getDouble("cantidad")
                    );
                    detalle.setNombreProducto(
                            rs.getString("nombre_producto"));
                    detalle.setUnidadMedida(
                            rs.getString("unidad_medida"));
                    detalles.add(detalle);
                }
            }
            return detalles;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo cargar el detalle de la orden.", e);
        }
    }

    public int registrar(
            Trabajador trabajador,
            String descripcionTrabajo,
            List<DetalleOrdenTrabajo> detalles) {
        validarEntrada(trabajador, descripcionTrabajo, detalles);

        String sqlOrden = """
                INSERT INTO ordenes_trabajo (trabajador_id, descripcion_trabajo)
                VALUES (?, ?)
                """;
        String sqlProducto = "SELECT nombre, costo_promedio FROM productos WHERE id = ?";
        String sqlDescontarStock = """
                UPDATE productos
                SET stock_actual = stock_actual - ?
                WHERE id = ? AND stock_actual >= ?
                """;
        String sqlDetalle = """
                INSERT INTO ordenes_trabajo_detalle
                    (orden_trabajo_id, producto_id, cantidad)
                VALUES (?, ?, ?)
                """;
        String sqlMovimiento = """
                INSERT INTO movimientos_inventario
                    (producto_id, cantidad_afectada, tipo_movimiento,
                     costo_unitario, responsable)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (conn) {
            conn.setAutoCommit(false);
            int ordenId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, trabajador.getId());
                pstmt.setString(2, descripcionTrabajo.trim());
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se pudo obtener el ID de la orden.");
                    }
                    ordenId = rs.getInt(1);
                }
            }

            String concepto = "Salida a Taller - " + trabajador.getNombre();
            String responsable = "ORDEN_TRABAJO_ID_" + ordenId;

            for (DetalleOrdenTrabajo detalle : detalles) {
                double costoUnitario;
                try (PreparedStatement pstmt = conn.prepareStatement(sqlProducto)) {
                    pstmt.setInt(1, detalle.getProductoId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El producto no existe: "
                                    + detalle.getNombreProducto());
                        }
                        costoUnitario = rs.getDouble("costo_promedio");
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlDescontarStock)) {
                    pstmt.setDouble(1, detalle.getCantidadUtilizada());
                    pstmt.setInt(2, detalle.getProductoId());
                    pstmt.setDouble(3, detalle.getCantidadUtilizada());
                    if (pstmt.executeUpdate() != 1) {
                        throw new IllegalStateException("Stock insuficiente para: "
                                + detalle.getNombreProducto());
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
                    pstmt.setInt(1, ordenId);
                    pstmt.setInt(2, detalle.getProductoId());
                    pstmt.setDouble(3, detalle.getCantidadUtilizada());
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlMovimiento)) {
                    pstmt.setInt(1, detalle.getProductoId());
                    pstmt.setDouble(2, -detalle.getCantidadUtilizada());
                    pstmt.setString(3, concepto);
                    pstmt.setDouble(4, costoUnitario);
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
                    "No se pudo registrar la orden. No se aplicaron cambios.", e);
        }
    }

    private void validarEntrada(
            Trabajador trabajador,
            String descripcionTrabajo,
            List<DetalleOrdenTrabajo> detalles) {
        if (trabajador == null || trabajador.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un trabajador.");
        }
        if (descripcionTrabajo == null || descripcionTrabajo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la descripción del trabajo.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un material.");
        }
        for (DetalleOrdenTrabajo detalle : detalles) {
            if (detalle == null
                    || detalle.getProductoId() == null
                    || detalle.getCantidadUtilizada() == null
                    || !Double.isFinite(detalle.getCantidadUtilizada())
                    || detalle.getCantidadUtilizada() <= 0.0) {
                throw new IllegalArgumentException(
                        "Todas las cantidades deben ser mayores que cero.");
            }
        }
    }
}
