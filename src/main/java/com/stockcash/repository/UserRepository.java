package com.stockcash.repository;

import com.stockcash.model.User;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la entidad User.
 *
 * ¿Por qué una interface y no la clase concreta directamente?
 * AuthService y UserService dependen de ESTA ABSTRACCIÓN, no de UserRepositoryImpl.
 * Beneficios:
 *   1. Tests unitarios: se puede pasar un mock sin BD real (Fase 5)
 *   2. Flexibilidad: cambiar de JDBC a JPA sin tocar ningún Service
 *   3. Principio D de SOLID: Dependency Inversion
 */
public interface UserRepository {

    Optional<User> findById(Long id) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    List<User> findAll() throws SQLException;

    /** Inserta un nuevo usuario y retorna el ID generado por la BD. */
    Long save(User user) throws SQLException;

    void update(User user) throws SQLException;
    void deactivate(Long id) throws SQLException;

    // ── Gestión de seguridad de login ────────────────────────────────────────

    /** Incrementa en 1 el contador de intentos fallidos. */
    void incrementFailedAttempts(Long id) throws SQLException;

    /** Resetea a 0 el contador (login exitoso). */
    void resetFailedAttempts(Long id) throws SQLException;

    /** Bloquea la cuenta hasta la fecha/hora indicada (FR-AUTH-03). */
    void lockAccount(Long id, LocalDateTime lockedUntil) throws SQLException;

    // ── Validaciones de unicidad ──────────────────────────────────────────────

    boolean existsByUsername(String username) throws SQLException;

    /** Versión para edición: excluye al propio usuario de la búsqueda (FR-USR-08). */
    boolean existsByUsername(String username, Long excludeId) throws SQLException;
}
