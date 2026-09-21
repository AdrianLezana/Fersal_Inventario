-- 0. Habilitar llaves foráneas (Ejecutar siempre al conectar la BD en tu código Java)
PRAGMA foreign_keys = ON;

-- 1. Usuarios (Tabla base para la trazabilidad)
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

-- 2. Trabajadores
CREATE TABLE trabajadores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    activo INTEGER DEFAULT 1,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 3. Proveedores
CREATE TABLE proveedores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    rut TEXT,
    telefono TEXT,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 4. Categorías
CREATE TABLE categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 5. Productos (Inventario Principal)
CREATE TABLE productos (
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

-- 6. Órdenes de Compra
CREATE TABLE ordenes_compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    folio TEXT NOT NULL,
    proveedor_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    fecha_ingreso DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 7. Detalle Órdenes de Compra
CREATE TABLE detalle_oc (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    orden_compra_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad REAL NOT NULL,
    precio_unitario REAL NOT NULL,
    FOREIGN KEY (orden_compra_id) REFERENCES ordenes_compra(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- 8. Órdenes de Trabajo
CREATE TABLE ordenes_trabajo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trabajador_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    tipo_trabajo TEXT,
    FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 9. Detalle Órdenes de Trabajo
CREATE TABLE detalle_ot (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    orden_trabajo_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad_utilizada REAL NOT NULL,
    FOREIGN KEY (orden_trabajo_id) REFERENCES ordenes_trabajo(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);