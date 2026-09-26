package com.stockcash.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga la configuración de la aplicación desde database.properties.
 * Las credenciales NUNCA se hardcodean en código — siempre desde el archivo externo.
 * El archivo database.properties está en .gitignore (NFR-SEC-04).
 */
public final class AppConfig {

    private static final Logger     log         = LoggerFactory.getLogger(AppConfig.class);
    private static final String     CONFIG_FILE = "config/database.properties";
    private static volatile Properties properties;

    private AppConfig() {}

    public static Properties getProperties() {
        if (properties == null) {
            synchronized (AppConfig.class) {
                if (properties == null) {
                    properties = load();
                }
            }
        }
        return properties;
    }

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new RuntimeException(
                    "No se encontró '" + CONFIG_FILE + "'.\n" +
                    "Copie database.properties.example como database.properties " +
                    "y complete sus credenciales MySQL.");
            }
            props.load(input);
            log.info("Configuracion de base de datos cargada.");
        } catch (IOException e) {
            throw new RuntimeException("Error al leer configuración: " + e.getMessage(), e);
        }
        return props;
    }
}
