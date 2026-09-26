package com.stockcash.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Gestiona la navegación entre pantallas de la aplicación.
 *
 * Centraliza la carga de FXML en un único lugar.
 * Todos los Controllers usan SceneManager.switchTo*() para navegar —
 * nunca cargan FXML directamente.
 */
public final class SceneManager {

    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);

    private static Stage primaryStage;

    private SceneManager() {}

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void switchToLogin() {
        loadScene("login", "Stock Cash Manager — Login", 900, 580);
    }

    public static void switchToDashboard() {
        loadScene("dashboard", "Stock Cash Manager", 1280, 800);
    }

    private static void loadScene(String fxmlName, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/fxml/" + fxmlName + ".fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, w, h);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
            log.info("Pantalla cargada: {}", fxmlName);
        } catch (IOException e) {
            log.error("Error al cargar pantalla '{}': {}", fxmlName, e.getMessage(), e);
            throw new RuntimeException("Error al cargar la pantalla: " + fxmlName, e);
        }
    }

    public static Stage getStage() { return primaryStage; }
}
