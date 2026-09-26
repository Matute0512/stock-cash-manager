package com.stockcash.service;

import com.stockcash.exception.AccountInactiveException;
import com.stockcash.exception.AccountLockedException;
import com.stockcash.exception.AuthenticationException;
import com.stockcash.exception.ValidationException;
import com.stockcash.model.User;
import com.stockcash.model.enums.AuditEventType;
import com.stockcash.repository.UserRepository;
import com.stockcash.repository.impl.UserRepositoryImpl;
import com.stockcash.security.PasswordHasher;
import com.stockcash.security.UserSession;
import com.stockcash.util.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de autenticación — coordina el flujo completo de login/logout.
 *
 * Flujo de login (CU-AUTH-01):
 *   1. Valida que los campos no estén vacíos
 *   2. Busca el usuario por username en BD
 *   3. Verifica estado (activo, bloqueado)
 *   4. Verifica contraseña con BCrypt
 *   5. Gestiona intentos fallidos y bloqueo
 *   6. Crea UserSession en memoria
 *   7. Registra en auditoría
 *
 * NOTA: este servicio NO verifica permisos con PermissionChecker
 * porque es el que CREA la sesión — no hay sesión previa que verificar.
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepo;
    private final AuditService   auditService;

    public AuthService() {
        this.userRepo     = new UserRepositoryImpl();
        this.auditService = new AuditService();
    }

    // Constructor para inyección de dependencias (tests con mocks — Fase 5)
    public AuthService(UserRepository userRepo, AuditService auditService) {
        this.userRepo     = userRepo;
        this.auditService = auditService;
    }

    /**
     * Autentica un usuario y crea la sesión en memoria.
     *
     * @param username      Nombre de usuario ingresado
     * @param plainPassword Contraseña en texto plano (se descarta tras verificar)
     * @return              La sesión creada
     * @throws ValidationException       Si los campos están vacíos
     * @throws AuthenticationException   Si las credenciales son incorrectas
     * @throws AccountLockedException    Si la cuenta está bloqueada
     * @throws AccountInactiveException  Si la cuenta está desactivada
     * @throws SQLException              Si hay error de comunicación con la BD
     */
    public UserSession login(String username, String plainPassword)
            throws AuthenticationException, AccountLockedException,
                   AccountInactiveException, ValidationException, SQLException {

        // 1. Validar formato básico de entrada
        InputValidator.requireNonBlank(username, "usuario");
        InputValidator.requireNonBlank(plainPassword, "contraseña");

        String cleanUsername = InputValidator.sanitize(username);

        // 2. Buscar usuario en BD
        Optional<User> userOpt = userRepo.findByUsername(cleanUsername);

        if (userOpt.isEmpty()) {
            // No revelar si el usuario existe o no — siempre "credenciales incorrectas"
            auditService.logSystem(AuditEventType.AUTH_LOGIN_FAILED,
                "Intento de login con usuario inexistente: " + cleanUsername);
            throw new AuthenticationException(
                "Credenciales incorrectas. Verificá usuario y contraseña.", 0);
        }

        User user = userOpt.get();

        // 3. Verificar si la cuenta está bloqueada (FR-AUTH-03)
        if (user.isLocked()) {
            log.warn("Intento de login en cuenta bloqueada: {}", cleanUsername);
            throw new AccountLockedException(user.getLockedUntil());
        }

        // 4. Verificar si la cuenta está activa
        if (!user.isActive()) {
            auditService.log(user.getId(), AuditEventType.AUTH_LOGIN_FAILED,
                "Intento de login en cuenta inactiva.");
            throw new AccountInactiveException();
        }

        // 5. Verificar contraseña con BCrypt
        if (!PasswordHasher.verify(plainPassword, user.getPasswordHash())) {
            handleFailedAttempt(user);
        }

        // 6. Login exitoso — resetear intentos y crear sesión
        userRepo.resetFailedAttempts(user.getId());

        UserSession session = UserSession.create(
            user.getId(), user.getUsername(), user.getFullName(), user.getRole()
        );

        auditService.log(user.getId(), AuditEventType.AUTH_LOGIN_SUCCESS,
            "Login exitoso. Rol: " + user.getRole().getDisplayName());

        log.info("Login exitoso: {} ({})", user.getUsername(), user.getRole());
        return session;
    }

    /**
     * Cierra la sesión activa y registra el evento en auditoría (FR-AUTH-06).
     */
    public void logout() {
        if (UserSession.isActive()) {
            UserSession session = UserSession.getInstance();
            auditService.log(session.getUserId(), AuditEventType.AUTH_LOGOUT,
                "Sesión cerrada correctamente.");
            log.info("Logout: {}", session.getUsername());
            UserSession.destroy();
        }
    }

    /**
     * Cambia la contraseña del usuario autenticado (FR-USR-05).
     * Verifica la contraseña actual antes de permitir el cambio.
     */
    public void changePassword(Long userId, String currentPassword, String newPassword)
            throws AuthenticationException, ValidationException, SQLException {

        InputValidator.validatePassword(newPassword);

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ValidationException("Usuario no encontrado."));

        if (!PasswordHasher.verify(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("La contraseña actual es incorrecta.", 0);
        }

        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepo.update(user);

        auditService.log(userId, AuditEventType.USER_PASSWORD_CHANGED,
            "Contraseña actualizada correctamente.");
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void handleFailedAttempt(User user)
            throws AuthenticationException, AccountLockedException, SQLException {

        int newAttempts = user.getFailedAttempts() + 1;
        userRepo.incrementFailedAttempts(user.getId());

        auditService.log(user.getId(), AuditEventType.AUTH_LOGIN_FAILED,
            "Intento fallido " + newAttempts + "/" + User.MAX_FAILED_ATTEMPTS);

        if (newAttempts >= User.MAX_FAILED_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(User.LOCKOUT_MINUTES);
            userRepo.lockAccount(user.getId(), lockUntil);

            auditService.log(user.getId(), AuditEventType.AUTH_ACCOUNT_LOCKED,
                "Cuenta bloqueada hasta " + lockUntil);

            log.warn("Cuenta bloqueada por intentos: {} hasta {}", user.getUsername(), lockUntil);
            throw new AccountLockedException(lockUntil);
        }

        int remaining = User.MAX_FAILED_ATTEMPTS - newAttempts;
        throw new AuthenticationException(
            "Credenciales incorrectas. Intentos restantes: " + remaining, remaining);
    }
}
