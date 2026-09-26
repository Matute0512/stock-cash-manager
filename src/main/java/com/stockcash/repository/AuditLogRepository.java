package com.stockcash.repository;

import com.stockcash.model.AuditLog;
import com.stockcash.model.enums.AuditEventType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de acceso a datos para el log de auditoría.
 * RESTRICCIÓN CRÍTICA: solo tiene operaciones de INSERT y SELECT.
 * NUNCA UPDATE ni DELETE — el log es inmutable (FR-AUD-04).
 */
public interface AuditLogRepository {

    /** Registra un nuevo evento de auditoría. Operación de solo escritura. */
    void log(Long userId, AuditEventType eventType, String description) throws SQLException;

    List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to) throws SQLException;
    List<AuditLog> findByEventType(AuditEventType eventType) throws SQLException;
    List<AuditLog> findByUserId(Long userId) throws SQLException;
}
