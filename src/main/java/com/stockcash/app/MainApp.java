package com.stockcash.app;

import com.stockcash.config.DatabaseConnection;
import com.stockcash.security.UserSession;
import com.stockcash.util.AlertHelper;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada de la aplicación JavaFX.
 *
 * Responsabilidades al iniciar:
 *   1. Verificar que database.properties existe
 *   2. Verificar conexión a MySQL
 *   3. Inicializar SceneManager con el Stage principal
 *   4. Mostrar la pantalla de Login
 *
 * Al cerrar:
 *   1. Destruir la sesión en memoria
 *   2. Cerrar la conexión a MySQL
 */
public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("=== Iniciando Stock Cash Manager v1.0.0 ===");

        // Verificar conexión a BD antes de mostrar la UI
        try {
            DatabaseConnection.getInstance();
        } catch (Exception e) {
            log.error("Error crítico al conectar a la base de datos: {}", e.getMessage());
            AlertHelper.showError(
                "Error de conexión",
                "No se pudo conectar a MySQL.\n\n" +
                "Verificá que:\n" +
                "  1. MySQL está corriendo\n" +
                "  2. El archivo config/database.properties existe\n" +
                "  3. Las credenciales son correctas\n\n" +
                "Detalle: " + e.getMessage()
            );
            javafx.application.Platform.exit();
            return;
        }

        // Configurar ventana principal
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(580);

        SceneManager.init(primaryStage);
        SceneManager.switchToLogin();

        log.info("Aplicacion iniciada correctamente.");
    }

    @Override
    public void stop() {
        log.info("Cerrando aplicacion...");
        // Limpiar sesión en memoria
        if (UserSession.isActive()) UserSession.destroy();
        // Cerrar conexión a BD
        try {
            DatabaseConnection.getInstance().close();
        } catch (Exception ignored) {}
        log.info("Aplicacion cerrada.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
