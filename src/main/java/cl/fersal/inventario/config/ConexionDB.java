package cl.fersal.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Ruta donde se creará y leerá el archivo de la base de datos
    private static final String URL = "jdbc:sqlite:inventario_fersal.db";

    public static Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(URL);
            System.out.println("¡Conexión a SQLite establecida con éxito!");

            // Activar las llaves foráneas en SQLite
            conexion.createStatement().execute("PRAGMA foreign_keys = ON;");

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }
}