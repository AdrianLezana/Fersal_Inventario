package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.Trabajador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrabajadorDAO {
    public Trabajador insertar(Trabajador trabajador) {
        validar(trabajador);

        String sql = """
                INSERT INTO trabajadores (rut, nombre, cargo)
                VALUES (?, ?, ?)
                """;

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, trabajador.getRut().trim());
            pstmt.setString(2, trabajador.getNombre().trim());
            pstmt.setString(3, trabajador.getCargo().trim());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el ID del trabajador.");
                }
                trabajador.setId(rs.getInt(1));
            }

            return trabajador;
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                throw new IllegalArgumentException(
                        "Ya existe un trabajador con ese RUT.", e);
            }
            throw new IllegalStateException(
                    "No se pudo insertar el trabajador.", e);
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM trabajadores WHERE id = ?";
        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn; PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("foreign key")) {
                throw new IllegalStateException(
                        "No se puede eliminar el trabajador porque tiene "
                                + "órdenes de trabajo asociadas.",
                        e);
            }
            throw new IllegalStateException(
                    "No se pudo eliminar el trabajador.", e);
        }
    }

    public List<Trabajador> obtenerTodos() {
        String sql = "SELECT id, rut, nombre, cargo FROM trabajadores ORDER BY nombre";
        List<Trabajador> trabajadores = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                trabajadores.add(new Trabajador(
                        rs.getInt("id"),
                        rs.getString("rut"),
                        rs.getString("nombre"),
                        rs.getString("cargo")
                ));
            }
            return trabajadores;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar los trabajadores.", e);
        }
    }

    private void validar(Trabajador trabajador) {
        if (trabajador == null) {
            throw new IllegalArgumentException("El trabajador es obligatorio.");
        }
        if (trabajador.getRut() == null || trabajador.getRut().isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        if (trabajador.getNombre() == null || trabajador.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (trabajador.getCargo() == null || trabajador.getCargo().isBlank()) {
            throw new IllegalArgumentException("El cargo es obligatorio.");
        }
    }
}
