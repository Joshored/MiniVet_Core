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