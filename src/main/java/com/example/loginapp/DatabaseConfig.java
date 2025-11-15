package com.example.loginapp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String DB_URL = "jdbc:sqlite:minivet.db";
    private static HikariDataSource dataSource;

    static {
        try {
            initializeDatabase();
            setupDataSource();
            logger.info("Base de datos inicializada correctamente");
        } catch (Exception e) {
            logger.error("Error crítico al inicializar la base de datos", e);
            throw new RuntimeException("No se pudo inicializar la base de datos", e);
        }
    }

    private static void initializeDatabase() {
        logger.info("Inicializando base de datos...");

        try (Connection conn = getSingleConnection();
             Statement stmt = conn.createStatement()) {

            // Habilitar claves foráneas en SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            // Tabla de clientes
            String createClientesTable = """
                        CREATE TABLE IF NOT EXISTS clientes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT NOT NULL,
                            apellido_paterno TEXT NOT NULL,
                            apellido_materno TEXT,
                            telefono TEXT NOT NULL,
                            email TEXT,
                            direccion TEXT,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                        )
                    """;
            stmt.execute(createClientesTable);
            logger.info("Tabla 'clientes' verificada/creada");

            // Tabla de mascotas
            String createMascotasTable = """
                        CREATE TABLE IF NOT EXISTS mascotas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nombre TEXT NOT NULL,
                            especie TEXT NOT NULL,
                            edad INTEGER NOT NULL,
                            raza TEXT,
                            sexo TEXT NOT NULL,
                            color TEXT,
                            numero_chip TEXT,
                            esterilizado BOOLEAN DEFAULT FALSE,
                            sintomas TEXT,
                            cliente_id INTEGER NOT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE
                        )
                    """;
            stmt.execute(createMascotasTable);
            logger.info("Tabla 'mascotas' verificada/creada");

            // Tabla de citas
            String createCitasTable = """
                        CREATE TABLE IF NOT EXISTS citas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            cliente_id INTEGER NOT NULL,
                            mascota_id INTEGER NOT NULL,
                            tipo_servicio TEXT NOT NULL,
                            fecha DATE NOT NULL,
                            hora_inicio TEXT NOT NULL,
                            hora_fin TEXT,
                            sintomas TEXT,
                            descripcion TEXT,
                            veterinario TEXT NOT NULL,
                            estado TEXT DEFAULT 'Programada',
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
                            FOREIGN KEY (mascota_id) REFERENCES mascotas (id) ON DELETE CASCADE
                        )
                    """;
            stmt.execute(createCitasTable);
            logger.info("Tabla 'citas' verificada/creada");

            // Tabla de productos
            String createProductosTable = """
                        CREATE TABLE IF NOT EXISTS productos (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            codigo TEXT UNIQUE NOT NULL,
                            nombre TEXT NOT NULL,
                            descripcion TEXT,
                            categoria TEXT NOT NULL,
                            stock INTEGER DEFAULT 0,
                            stock_minimo INTEGER DEFAULT 5,
                            precio_compra REAL DEFAULT 0,
                            precio_venta REAL DEFAULT 0,
                            proveedor TEXT,
                            fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
                        )
                    """;
            stmt.execute(createProductosTable);
            logger.info("Tabla 'productos' verificada/creada");

            // Insertar algunos productos de ejemplo
            String insertSampleProducts = """
                        INSERT OR IGNORE INTO productos (codigo, nombre, categoria, stock, stock_minimo, precio_compra, precio_venta, proveedor) 
                        VALUES 
                        ('MED-001', 'Antiparasitario para perros', 'Medicamentos', 15, 5, 45.00, 75.00, 'Lab. Veterinario S.A.'),
                        ('ALI-001', 'Alimento premium para gatos', 'Alimentos', 8, 10, 120.00, 180.00, 'PetFood Corp'),
                        ('ACC-001', 'Correa ajustable', 'Accesorios', 25, 5, 30.00, 50.00, 'Accesorios Pet'),
                        ('MED-002', 'Vacuna antirrábica', 'Vacunas', 3, 5, 80.00, 120.00, 'Lab. BioVet'),
                        ('HIG-001', 'Shampoo antipulgas', 'Higiene', 12, 8, 25.00, 40.00, 'Higiene Animal')
                    """;
            stmt.execute(insertSampleProducts);

            // Tabla de facturas
            String createFacturasTable = """
                        CREATE TABLE IF NOT EXISTS facturas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            numero_factura TEXT UNIQUE NOT NULL,
                            fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
                            cliente_id INTEGER NOT NULL,
                            metodo_pago TEXT NOT NULL,
                            estado TEXT DEFAULT 'Pendiente',
                            subtotal REAL DEFAULT 0,
                            iva REAL DEFAULT 0,
                            total REAL DEFAULT 0,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE
                        )
                    """;
            stmt.execute(createFacturasTable);
            logger.info("Tabla 'facturas' verificada/creada");

            // Tabla de detalles_factura
            String createDetallesFacturaTable = """
                        CREATE TABLE IF NOT EXISTS detalles_factura (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            factura_id INTEGER NOT NULL,
                            producto_id INTEGER NOT NULL,
                            cantidad INTEGER NOT NULL,
                            precio_unitario REAL NOT NULL,
                            subtotal REAL NOT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (factura_id) REFERENCES facturas (id) ON DELETE CASCADE,
                            FOREIGN KEY (producto_id) REFERENCES productos (id) ON DELETE CASCADE
                        )
                    """;
            stmt.execute(createDetallesFacturaTable);
            logger.info("Tabla 'detalles_factura' verificada/creada");

            // Insertar algunas facturas de ejemplo
            String insertSampleFacturas = """
                        INSERT OR IGNORE INTO facturas (numero_factura, cliente_id, metodo_pago, estado, subtotal, iva, total) 
                        VALUES 
                        ('FAC-001', 1, 'Efectivo', 'Pagada', 180.00, 28.80, 208.80),
                        ('FAC-002', 2, 'Tarjeta', 'Pagada', 120.00, 19.20, 139.20)
                    """;
            stmt.execute(insertSampleFacturas);

            // Insertar detalles de ejemplo
            String insertSampleDetalles = """
                        INSERT OR IGNORE INTO detalles_factura (factura_id, producto_id, cantidad, precio_unitario, subtotal) 
                        VALUES 
                        (1, 1, 2, 180.00, 360.00),
                        (1, 3, 1, 50.00, 50.00),
                        (2, 2, 1, 75.00, 75.00)
                    """;
            stmt.execute(insertSampleDetalles);

            // Tabla de usuarios (para login)
            String createUsuariosTable = """
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE NOT NULL,
                            password TEXT NOT NULL,
                            email TEXT,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                        )
                    """;
            stmt.execute(createUsuariosTable);
            logger.info("Tabla 'usuarios' verificada/creada");

            // Insertar usuario por defecto si no existe
            String insertDefaultUser = """
                         INSERT OR IGNORE INTO usuarios (username, password, email)\s
                         VALUES ('admin', 'admin123', 'admin@minivet.com')
                    \s""";
            int inserted = stmt.executeUpdate(insertDefaultUser);
            if (inserted > 0) {
                logger.info("Usuario por defecto creado: admin/admin123");
            } else {
                logger.info("Usuario por defecto ya existe");
            }

            // Verificar que el usuario se creó
            String checkUser = "SELECT COUNT(*) FROM usuarios WHERE username = 'admin'";
            var rs = stmt.executeQuery(checkUser);
            if (rs.next() && rs.getInt(1) > 0) {
                logger.info("Usuario admin verificado en la base de datos");
            }

        } catch (SQLException e) {
            logger.error("Error al inicializar la base de datos", e);
            throw new RuntimeException("Error initializing database", e);
        }
    }

    private static void setupDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setDriverClassName("org.sqlite.JDBC");

            dataSource = new HikariDataSource(config);

            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Conexión a la base de datos probada exitosamente");
            }

        } catch (SQLException e) {
            logger.error("Error al configurar el DataSource", e);
            throw new RuntimeException("Error setting up data source", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource no está disponible");
        }
        return dataSource.getConnection();
    }

    private static Connection getSingleConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite no encontrado", e);
        }
        return java.sql.DriverManager.getConnection(DB_URL);
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("DataSource cerrado");
        }
    }

    public static boolean isDatabaseConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Error verificando conexión a la base de datos", e);
            return false;
        }
    }
}