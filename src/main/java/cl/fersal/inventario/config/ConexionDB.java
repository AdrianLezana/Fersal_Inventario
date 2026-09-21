package cl.fersal.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDB {

    private static final String URL = "jdbc:sqlite:inventario_fersal.db";

    public static Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(URL);
            // Activar las llaves foráneas en SQLite
            conexion.createStatement().execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    /**
     * Este método construye la estructura de la base de datos si es que el archivo es nuevo.
     * Utiliza "IF NOT EXISTS" por lo que es seguro llamarlo cada vez que inicie el programa.
     */
    public static void inicializarBaseDeDatos() {
        String sqlEsquema = """
            -- 1. Tabla de Usuarios
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL
            );

            -- 2. Tabla de Categorías (Opcional por ahora, pero necesaria para la llave foránea)
            CREATE TABLE IF NOT EXISTS categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                usuario_id INTEGER,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            );

            -- 3. Tabla Principal de Productos
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo_interno TEXT,
                categoria_id INTEGER,
                nombre TEXT NOT NULL,
                dimensiones TEXT,
                unidad_medida TEXT NOT NULL,
                ubicacion TEXT,
                stock_actual REAL DEFAULT 0.0,
                precio_venta REAL DEFAULT 0.0,
                precio_venta_metro REAL,
                precio_trabajado_metro REAL,
                costo_promedio REAL DEFAULT 0.0,
                usuario_id INTEGER NOT NULL,
                FOREIGN KEY (categoria_id) REFERENCES categorias(id),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            );
            """;

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {

            // Ejecutamos todo el bloque SQL
            stmt.executeUpdate(sqlEsquema);
            System.out.println("Esquema de base de datos verificado/inicializado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}