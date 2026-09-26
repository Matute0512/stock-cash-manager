package com.stockcash.service;

import com.stockcash.model.enums.AuditEventType;
import com.stockcash.repository.AuditLogRepository;
import com.stockcash.repository.impl.AuditLogRepositoryImpl;
import com.stockcash.security.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Servicio de auditoría — registra eventos críticos del sistema (FR-AUD-01).
 *
 * Diseño: nunca lanza excepciones hacia arriba.
 * Si falla el log de auditoría, se registra en el log técnico (Logback)
 * pero NO interrumpe la operación principal. El negocio no puede detenerse
 * por un fallo de auditoría — pero el fallo siempre queda registrado.
 */
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditRepo;

    public AuditService() {
        this.auditRepo = new AuditLogRepositoryImpl();
    }

    public AuditService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Registra un evento asociado al usuario de la sesión activa.
     * Uso más común: log del usuario que acaba de operar.
     */
    public void log(AuditEventType eventType, String description) {
        Long userId = UserSession.isActive() ? UserSession.getInstance().getUserId() : null;
        logInternal(userId, eventType, description);
    }

    /**
     * Registra un evento con usuario explícito.
     * Uso: login (la sesión aún no existe), o eventos de otro usuario.
     */
    public void log(Long userId, AuditEventType eventType, String description) {
        logInternal(userId, eventType, description);
    }

    /** Registra un evento del sistema sin usuario asociado. */
    public void logSystem(AuditEventType eventType, String description) {
        logInternal(null, eventType, description);
    }

    private void logInternal(Long userId, AuditEventType eventType, String description) {
        try {
            auditRepo.log(userId, eventType, description);
        } catch (SQLException e) {
            // El fallo de auditoría NO detiene la operación (ver Javadoc de clase)
            log.error("FALLO AL REGISTRAR AUDITORIA [{} - {}]: {}",
                    eventType, description, e.getMessage());
        }
    }
}
