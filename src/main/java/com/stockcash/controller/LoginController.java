package com.stockcash.controller;

import com.stockcash.app.SceneManager;
import com.stockcash.exception.AccountInactiveException;
import com.stockcash.exception.AccountLockedException;
import com.stockcash.exception.AuthenticationException;
import com.stockcash.service.AuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Login.
 *
 * Responsabilidades:
 *   - Capturar evento del botón "Ingresar"
 *   - Delegar autenticación al AuthService
 *   - Mostrar mensajes de error al usuario
 *   - Redirigir al Dashboard si el login es exitoso
 *
 * NO CONTIENE LÓGICA DE NEGOCIO — todo está en AuthService.
 */
public class LoginController implements Initializable {

    @FXML private TextField    usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         messageLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Deshabilitar el botón si algún campo está vacío (UX inmediata)
        loginButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
        );

        // Limpiar el mensaje de error cuando el usuario escribe de nuevo
        usernameField.textProperty().addListener((obs, o, n) -> hideMessage());
        passwordField.textProperty().addListener((obs, o, n) -> hideMessage());
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        hideMessage();
        setLoading(true);

        /*
         * ¿Por qué Task y no llamar directo?
         * AuthService accede a la BD — puede tardar varios cientos de ms.
         * Si lo llamáramos en el hilo de la UI (JavaFX Application Thread),
         * la ventana se congelaría hasta que termine.
         * Task<> ejecuta en un hilo separado y notifica a la UI al terminar.
         */
        Task<Void> loginTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                authService.login(username, password);
                return null;
            }
        };

        loginTask.setOnSucceeded(e -> SceneManager.switchToDashboard());

        loginTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = loginTask.getException();

            if (ex instanceof AccountLockedException ale) {
                String until = ale.getLockedUntil()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                showError("🔒 Cuenta bloqueada hasta las " + until +
                          ". Intente nuevamente más tarde.");

            } else if (ex instanceof AccountInactiveException) {
                showError("Tu cuenta está desactivada. Contactá al administrador.");

            } else if (ex instanceof AuthenticationException ae) {
                String msg = ae.getRemainingAttempts() > 0
                    ? ae.getMessage()
                    : "Credenciales incorrectas.";
                showError(msg);

            } else {
                // Error técnico (BD desconectada, etc.)
                showError("Error de conexión. Verificá que MySQL esté activo.");
            }
        });

        new Thread(loginTask, "login-thread").start();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Verificando..." : "Ingresar");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
