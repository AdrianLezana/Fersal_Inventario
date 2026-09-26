package cl.fersal.inventario.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Path;

public class ConexionDB {

    // Obtenemos la ruta a la carpeta personal del usuario en Windows
    private static final String CARPETA_APP = System.getProperty("user.home") + File.separator + "FersalInventario";

    // Apuntamos la base de datos a esa nueva carpeta segura
    private static final String URL = "jdbc:sqlite:" + CARPETA_APP + File.separator + "inventario_fersal.db";

    public static Path obtenerRutaBaseDatos() {
        return Path.of(CARPETA_APP, "inventario_fersal.db");
    }

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
            try (PreparedStatement pragma = conexion.prepareStatement(
                    "PRAGMA foreign_keys = ON")) {
                pragma.execute();
            }
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
                estado TEXT NOT NULL DEFAULT 'ACTIVO',
                usuario_id INTEGER NOT NULL,
                FOREIGN KEY (categoria_id) REFERENCES categorias(id),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            );

            -- 4. Historial inmutable de movimientos de inventario
            CREATE TABLE IF NOT EXISTS movimientos_inventario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                producto_id INTEGER NOT NULL,
                cantidad_afectada REAL NOT NULL,
                tipo_movimiento TEXT NOT NULL,
                costo_unitario REAL NOT NULL DEFAULT 0.0,
                responsable TEXT NOT NULL,
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            );

            -- 5. Trabajadores que solicitan materiales
            CREATE TABLE IF NOT EXISTS trabajadores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                rut TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                cargo TEXT NOT NULL
            );

            -- 6. Cabecera de las órdenes de trabajo
            CREATE TABLE IF NOT EXISTS ordenes_trabajo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                trabajador_id INTEGER NOT NULL,
                descripcion_trabajo TEXT NOT NULL,
                estado TEXT NOT NULL DEFAULT 'ACTIVA',
                FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id)
            );

            -- 7. Materiales consumidos en cada orden
            CREATE TABLE IF NOT EXISTS ordenes_trabajo_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orden_trabajo_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad REAL NOT NULL CHECK (cantidad > 0),
                FOREIGN KEY (orden_trabajo_id) REFERENCES ordenes_trabajo(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            );

            -- 8. Proveedores de inventario
            CREATE TABLE IF NOT EXISTS proveedores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                rut TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                telefono TEXT,
                email TEXT
            );

            -- 9. Cabecera de las órdenes de compra
            CREATE TABLE IF NOT EXISTS ordenes_compra (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                proveedor_id INTEGER NOT NULL,
                numero_documento TEXT NOT NULL,
                neto REAL NOT NULL CHECK (neto >= 0),
                iva REAL NOT NULL CHECK (iva >= 0),
                total REAL NOT NULL CHECK (total >= 0),
                estado TEXT NOT NULL DEFAULT 'ACTIVA',
                FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
            );

            -- 10. Productos incluidos en cada orden de compra
            CREATE TABLE IF NOT EXISTS detalle_orden_compra (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orden_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad REAL NOT NULL CHECK (cantidad > 0),
                precio_unitario REAL NOT NULL CHECK (precio_unitario >= 0),
                FOREIGN KEY (orden_id) REFERENCES ordenes_compra(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            );

            -- Impide modificar o eliminar movimientos una vez registrados.
            CREATE TRIGGER IF NOT EXISTS impedir_actualizacion_movimiento
            BEFORE UPDATE ON movimientos_inventario
            BEGIN
                SELECT RAISE(ABORT, 'Los movimientos de inventario son inmutables');
            END;

            CREATE TRIGGER IF NOT EXISTS impedir_eliminacion_movimiento
            BEFORE DELETE ON movimientos_inventario
            BEGIN
                SELECT RAISE(ABORT, 'Los movimientos de inventario son inmutables');
            END;
            """;

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {

            // Ejecutamos todo el bloque SQL usando un Statement normal
            stmt.executeUpdate(sqlEsquema);
            asegurarColumnaEstadoProducto(conn);
            asegurarColumnaEstadoOrdenTrabajo(conn);
            asegurarColumnaEstadoOrdenCompra(conn);
            System.out.println("Esquema de base de datos verificado/inicializado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    private static void asegurarColumnaEstadoProducto(Connection conn)
            throws SQLException {
        boolean existeEstado = false;
        String sqlColumnas = "PRAGMA table_info(productos)";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlColumnas)) {
            while (rs.next()) {
                if ("estado".equalsIgnoreCase(rs.getString("name"))) {
                    existeEstado = true;
                    break;
                }
            }
        }

        if (!existeEstado) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "ALTER TABLE productos "
                                + "ADD COLUMN estado TEXT "
                                + "NOT NULL DEFAULT 'ACTIVO'"
                );
            }
        }
    }

    private static void asegurarColumnaEstadoOrdenTrabajo(Connection conn)
            throws SQLException {
        boolean existeEstado = false;
        String sqlColumnas = "PRAGMA table_info(ordenes_trabajo)";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlColumnas)) {
            while (rs.next()) {
                if ("estado".equalsIgnoreCase(rs.getString("name"))) {
                    existeEstado = true;
                    break;
                }
            }
        }

        if (!existeEstado) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "ALTER TABLE ordenes_trabajo "
                                + "ADD COLUMN estado TEXT "
                                + "NOT NULL DEFAULT 'ACTIVA'"
                );
            }
        }
    }

    private static void asegurarColumnaEstadoOrdenCompra(Connection conn)
            throws SQLException {
        boolean existeEstado = false;
        String sqlColumnas = "PRAGMA table_info(ordenes_compra)";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlColumnas)) {
            while (rs.next()) {
                if ("estado".equalsIgnoreCase(rs.getString("name"))) {
                    existeEstado = true;
                    break;
                }
            }
        }

        if (!existeEstado) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "ALTER TABLE ordenes_compra "
                                + "ADD COLUMN estado TEXT "
                                + "NOT NULL DEFAULT 'ACTIVA'"
                );
            }
        }
    }
}