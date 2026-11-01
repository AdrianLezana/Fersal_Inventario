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

    private static final String PATRON_RUT = "^[0-9]{7,8}-[0-9kK]{1}$";
    private static final String PATRON_EMAIL =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PATRON_TELEFONO = "^(\\+?56)?9\\d{8}$";

    public Proveedor crear(Proveedor proveedor) {
        validar(proveedor);

        String sql = """
                INSERT INTO proveedores
                    (rut, razon_social, nombre, giro, telefono, email, direccion)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            setDatosProveedor(pstmt, proveedor);
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
            throw errorPersistencia(e, "insertar");
        }
    }

    public Proveedor insertar(Proveedor proveedor) {
        return crear(proveedor);
    }

    public boolean actualizar(Proveedor proveedor) {
        validar(proveedor);
        if (proveedor.getId() == null) {
            throw new IllegalArgumentException(
                    "Seleccione un proveedor válido para actualizar.");
        }

        String sql = """
                UPDATE proveedores
                SET rut = ?, razon_social = ?, nombre = ?, giro = ?,
                    telefono = ?, email = ?, direccion = ?
                WHERE id = ?
                """;
        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setDatosProveedor(pstmt, proveedor);
            pstmt.setInt(8, proveedor.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw errorPersistencia(e, "actualizar");
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("foreign key")) {
                throw new IllegalStateException(
                        "No se puede eliminar el proveedor porque tiene compras registradas.",
                        e);
            }
            throw new IllegalStateException(
                    "No se pudo eliminar el proveedor.", e);
        }
    }

    public List<Proveedor> obtenerTodos() {
        String sql = """
                SELECT id, rut, razon_social, giro, telefono, email, direccion
                FROM proveedores
                ORDER BY razon_social
                """;
        List<Proveedor> proveedores = new ArrayList<>();

        Connection conn = ConexionDB.conectar();
        if (conn == null) {
            throw new IllegalStateException(
                    "No se pudo conectar a la base de datos.");
        }
        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setId(rs.getInt("id"));
                proveedor.setRut(rs.getString("rut"));
                proveedor.setRazonSocial(rs.getString("razon_social"));
                proveedor.setGiro(rs.getString("giro"));
                proveedor.setTelefono(rs.getString("telefono"));
                proveedor.setEmail(rs.getString("email"));
                proveedor.setDireccion(rs.getString("direccion"));
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
        String rut = proveedor.getRut().trim();
        if (!rut.matches(PATRON_RUT)) {
            throw new IllegalArgumentException(
                    "El RUT debe tener formato 12345678-9 o 12345678-K, sin puntos.");
        }
        if (proveedor.getRazonSocial() == null
                || proveedor.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException(
                    "La razón social es obligatoria.");
        }

        String email = textoOpcional(proveedor.getEmail());
        if (email != null && !email.matches(PATRON_EMAIL)) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        }
        String telefono = textoOpcional(proveedor.getTelefono());
        if (telefono != null && !telefono.matches(PATRON_TELEFONO)) {
            throw new IllegalArgumentException(
                    "El teléfono debe tener formato 912345678, 56912345678 o +56912345678.");
        }
    }

    private void setDatosProveedor(
            PreparedStatement pstmt,
            Proveedor proveedor) throws SQLException {
        String rut = proveedor.getRut().trim();
        String razonSocial = proveedor.getRazonSocial().trim();
        pstmt.setString(1, rut);
        pstmt.setString(2, razonSocial);
        pstmt.setString(3, razonSocial);
        pstmt.setString(4, textoOpcional(proveedor.getGiro()));
        pstmt.setString(5, textoOpcional(proveedor.getTelefono()));
        pstmt.setString(6, textoOpcional(proveedor.getEmail()));
        pstmt.setString(7, textoOpcional(proveedor.getDireccion()));
    }

    private RuntimeException errorPersistencia(
            SQLException error,
            String accion) {
        if (error.getMessage() != null
                && error.getMessage().toLowerCase().contains("unique")) {
            return new IllegalArgumentException(
                    "Ya existe un proveedor con ese RUT.", error);
        }
        return new IllegalStateException(
                "No se pudo " + accion + " el proveedor.", error);
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
