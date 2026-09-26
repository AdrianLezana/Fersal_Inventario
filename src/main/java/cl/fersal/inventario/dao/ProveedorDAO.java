package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public Proveedor insertar(Proveedor proveedor) {
        validar(proveedor);

        String sql = """
                INSERT INTO proveedores (rut, nombre, telefono, email)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, proveedor.getRut().trim());
            pstmt.setString(2, proveedor.getNombre().trim());
            pstmt.setString(3, textoOpcional(proveedor.getTelefono()));
            pstmt.setString(4, textoOpcional(proveedor.getEmail()));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el ID del proveedor.");
                }
                proveedor.setId(rs.getInt(1));
            }

            return proveedor;
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                throw new IllegalArgumentException(
                        "Ya existe un proveedor con ese RUT.", e);
            }
            throw new IllegalStateException(
                    "No se pudo insertar el proveedor.", e);
        }
    }

    public List<Proveedor> obtenerTodos() {
        String sql = """
                SELECT id, rut, nombre, telefono, email
                FROM proveedores
                ORDER BY nombre
                """;
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setId(rs.getInt("id"));
                proveedor.setRut(rs.getString("rut"));
                proveedor.setNombre(rs.getString("nombre"));
                proveedor.setTelefono(rs.getString("telefono"));
                proveedor.setEmail(rs.getString("email"));
                proveedores.add(proveedor);
            }
            return proveedores;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar los proveedores.", e);
        }
    }

    private void validar(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException(
                    "El proveedor es obligatorio.");
        }
        if (proveedor.getRut() == null || proveedor.getRut().isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        if (proveedor.getNombre() == null
                || proveedor.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
