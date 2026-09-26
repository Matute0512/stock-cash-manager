package com.stockcash.repository.impl;

import com.stockcash.config.DatabaseConnection;
import com.stockcash.model.User;
import com.stockcash.model.enums.Role;
import com.stockcash.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de usuarios.
 *
 * REGLAS INQUEBRANTABLES de toda implementación Repository:
 *   1. 100% PreparedStatement — NUNCA concatenación de strings en SQL (NFR-SEC-02)
 *   2. NUNCA contiene lógica de negocio — solo traduce entre objetos Java y BD
 *   3. Cierra siempre los ResultSet y Statement en bloques try-with-resources
 *   4. Propaga SQLException — el Service decide cómo manejarla
 */
public class UserRepositoryImpl implements UserRepository {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        // LOWER() en ambos lados: búsqueda case-insensitive (FR-USR-08)
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        }
        return users;
    }

    @Override
    public Long save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, password_hash, full_name, role, active)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername().toLowerCase());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("No se obtuvo ID generado para el nuevo usuario.");
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = """
            UPDATE users
            SET full_name = ?, role = ?, active = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole().name());
            ps.setBoolean(3, user.isActive());
            ps.setLong(4, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deactivate(Long id) throws SQLException {
        // Baja lógica — nunca DELETE (BR-01)
        String sql = "UPDATE users SET active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void incrementFailedAttempts(Long id) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void resetFailedAttempts(Long id) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void lockAccount(Long id, LocalDateTime lockedUntil) throws SQLException {
        String sql = "UPDATE users SET locked_until = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(lockedUntil));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?) LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public boolean existsByUsername(String username, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?) AND id != ? LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Mapea una fila del ResultSet a un objeto User. */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("active"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));

        Timestamp lockedUntil = rs.getTimestamp("locked_until");
        if (lockedUntil != null) user.setLockedUntil(lockedUntil.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) user.setUpdatedAt(updatedAt.toLocalDateTime());

        return user;
    }
}
