package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.MovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KardexDAO {

    public List<MovimientoInventario> obtenerTodos() {
        String sql = """
                SELECT
                    m.id,
                    m.fecha,
                    m.producto_id,
                    p.nombre AS nombre_producto,
                    m.cantidad_afectada,
                    m.tipo_movimiento,
                    m.costo_unitario,
                    m.responsable
                FROM movimientos_inventario m
                INNER JOIN productos p ON p.id = m.producto_id
                ORDER BY m.fecha DESC, m.id DESC
                """;

        List<MovimientoInventario> movimientos = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setId(rs.getInt("id"));
                movimiento.setFecha(rs.getString("fecha"));
                movimiento.setProductoId(rs.getInt("producto_id"));
                movimiento.setNombreProducto(rs.getString("nombre_producto"));
                movimiento.setCantidadAfectada(rs.getDouble("cantidad_afectada"));
                movimiento.setTipoMovimiento(rs.getString("tipo_movimiento"));
                movimiento.setCostoUnitario(rs.getDouble("costo_unitario"));
                movimiento.setResponsable(rs.getString("responsable"));
                movimientos.add(movimiento);
            }

            return movimientos;
        } catch (SQLException e) {
            System.err.println("Error al obtener el Kardex: " + e.getMessage());
            throw new IllegalStateException(
                    "No se pudo cargar el historial de movimientos.", e);
        }
    }
}
