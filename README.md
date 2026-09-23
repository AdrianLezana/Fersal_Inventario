# Fersal Inventario

Sistema de escritorio para el control de inventario, diseñado a medida para la gestión de productos de vidriería (vidrios, perfiles, quincallería).

## Características Principales
* **Gestión de Productos (MVP):** CRUD completo con soporte para dimensiones, ubicación física, unidades de medida y múltiples capas de precios (costo, venta, venta por metro y trabajado).
* **Persistencia Local Segura:** Base de datos SQLite integrada. El sistema verifica e inicializa automáticamente su esquema y el usuario administrador en el directorio de usuario de Windows para evitar problemas de permisos.
* **Arquitectura Limpia:** Estructura modular basada en el patrón MVC (Model-View-Controller) apoyada por los patrones DAO (Data Access Object) y Service.
* **Verificador de Actualizaciones:** Módulo asíncrono (`java.net.http`) que consulta un archivo de versión en GitHub para notificar al usuario sobre nuevas versiones disponibles sin congelar la interfaz.
* **Despliegue Nativo:** Empaquetado como instalador `.exe` independiente, eliminando la necesidad de que el cliente instale Java.

## Tecnologías Utilizadas
* **Lenguaje:** Java (JDK 24)
* **Interfaz Gráfica:** JavaFX 21
* **Base de Datos:** SQLite (vía `sqlite-jdbc`)
* **Gestor de Dependencias:** Maven
* **Empaquetado Nativo:** `jpackage` + WiX Toolset v3.14

## Compilación y Empaquetado (Windows)
Este proyecto utiliza el patrón "Launcher Class" (ejecución basada en Classpath en lugar de Module Path) para garantizar la compatibilidad del compilador estricto de Java con librerías de bases de datos de terceros.

Para generar un nuevo instalador `.exe`:

1. Empaquetar las dependencias en `target/lib`:
   ```bash
   mvn clean package