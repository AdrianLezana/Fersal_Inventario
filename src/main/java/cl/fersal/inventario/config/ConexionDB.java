package cl.fersal.inventario.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDB {

    // Obtenemos la ruta a la carpeta personal del usuario en Windows
    private static final String CARPETA_APP = System.getProperty("user.home") + File.separator + "FersalInventario";

    // Apuntamos la base de datos a esa nueva carpeta segura
    private static final String URL = "jdbc:sqlite:" + CARPETA_APP + File.separator + "inventario_fersal.db";

    public static Connection conectar() {
        File directorio = new File(CARPETA_APP);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        Connection conexion = null;
        try {
            // Esta línea fuerza a Java a despertar el driver dentro del .exe
            Class.forName("org.sqlite.JDBC");

            conexion = DriverManager.getConnection(URL);
            conexion.createStatement().execute("PRAGMA foreign_keys = ON;");
        } catch (ClassNotFoundException e) {
            System.err.println("El driver de SQLite no se empacó correctamente: " + e.getMessage());
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