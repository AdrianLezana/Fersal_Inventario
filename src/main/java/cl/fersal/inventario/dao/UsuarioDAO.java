package cl.fersal.inventario.dao;

import cl.fersal.inventario.config.ConexionDB;
import cl.fersal.inventario.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos y le asigna el ID generado.
     */
    public Usuario crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash) VALUES (?, ?)";

        // El parámetro RETURN_GENERATED_KEYS nos permite recuperar el ID que SQLite le asignó
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getUsername());
            pstmt.setString(2, usuario.getPasswordHash());

            pstmt.executeUpdate();

            // Recuperar el ID autoincremental
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
            return usuario;

        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca un usuario por su nombre (Útil para el Login).
     */
    public Usuario obtenerPorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        Usuario usuario = null;

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por username: " + e.getMessage());
        }

        return usuario; // Retorna null si el usuario no existe
    }
}