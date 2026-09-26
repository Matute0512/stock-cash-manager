package com.stockcash.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Centraliza la creación de diálogos JavaFX (NFR-USA-02, NFR-USA-03).
 * Todos los mensajes al usuario pasan por aquí — facilita internacionalización futura.
 */
public final class AlertHelper {

    private AlertHelper() {}

    public static void showError(String title, String message) {
        show(AlertType.ERROR, title, message);
    }

    public static void showWarning(String title, String message) {
        show(AlertType.WARNING, title, message);
    }

    public static void showInfo(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    /**
     * Muestra un diálogo de confirmación y retorna true si el usuario confirma.
     * Usar para operaciones críticas: anulaciones, cierres de caja (NFR-USA-03).
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
