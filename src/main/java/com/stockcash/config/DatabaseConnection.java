package com.stockcash.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton thread-safe para la conexión a MySQL (Patrón Singleton con double-checked locking).
 *
 * ¿Por qué Singleton?
 * La conexión JDBC es un recurso costoso. Crear una nueva por cada operación
 * sería lento (~100ms por conexión) e inestable. Un Singleton garantiza
 * exactamente UNA conexión reutilizable con reconexión automática (NFR-REL-03).
 *
 * ¿Por qué 'volatile'?
 * Sin volatile, en un entorno multihilo, un hilo podría ver una instancia
 * parcialmente construida. volatile garantiza visibilidad completa entre hilos.
 */
public final class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        this.connection = createConnection();
        log.info("Conexion a MySQL establecida correctamente.");
    }

    /** Double-checked locking — thread-safe sin sincronizar siempre. */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Retorna la conexión activa.
     * Si detecta que está cerrada, reconecta automáticamente (NFR-REL-03).
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.warn("Conexion perdida. Reconectando...");
            connection = createConnection();
            log.info("Reconexion exitosa.");
        }
        return connection;
    }

    private Connection createConnection() throws SQLException {
        String url      = AppConfig.get("db.url");
        String username = AppConfig.get("db.username");
        String password = AppConfig.get("db.password");
        return DriverManager.getConnection(url, username, password);
    }

    /** Cierra la conexión de forma limpia al apagar la aplicación. */
    public void close() {
        if (connection != null) {
            try { connection.close(); }
            catch (SQLException e) { log.error("Error al cerrar conexion: {}", e.getMessage()); }
        }
        instance = null;
    }
}
