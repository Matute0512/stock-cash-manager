package com.stockcash.repository.impl;

import com.stockcash.config.DatabaseConnection;
import com.stockcash.model.AuditLog;
import com.stockcash.model.enums.AuditEventType;
import com.stockcash.repository.AuditLogRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC — solo INSERT y SELECT. Nunca UPDATE/DELETE (FR-AUD-04). */
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void log(Long userId, AuditEventType eventType, String description) throws SQLException {
        String sql = "INSERT INTO audit_log (user_id, event_type, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (userId != null) ps.setLong(1, userId); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, eventType.name());
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT al.*, u.username FROM audit_log al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "WHERE al.created_at BETWEEN ? AND ? ORDER BY al.created_at DESC";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) logs.add(mapRow(rs));
            }
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByEventType(AuditEventType eventType) throws SQLException {
        String sql = "SELECT al.*, u.username FROM audit_log al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "WHERE al.event_type = ? ORDER BY al.created_at DESC LIMIT 500";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, eventType.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) logs.add(mapRow(rs));
            }
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT al.*, u.username FROM audit_log al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "WHERE al.user_id = ? ORDER BY al.created_at DESC LIMIT 200";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) logs.add(mapRow(rs));
            }
        }
        return logs;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) log.setUserId(uid);
        log.setUsername(rs.getString("username"));
        log.setEventType(AuditEventType.valueOf(rs.getString("event_type")));
        log.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }
}
