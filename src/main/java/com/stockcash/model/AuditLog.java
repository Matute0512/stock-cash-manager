package com.stockcash.model;

import com.stockcash.model.enums.AuditEventType;
import java.time.LocalDateTime;

/** Registro de auditoría — INMUTABLE una vez creado (FR-AUD-04). */
public class AuditLog {

    private Long           id;
    private Long           userId;         // null para eventos del sistema
    private String         username;       // desnormalizado para lectura directa
    private AuditEventType eventType;
    private String         description;
    private LocalDateTime  createdAt;

    public AuditLog() {}

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public Long getUserId()                 { return userId; }
    public void setUserId(Long u)           { this.userId = u; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public AuditEventType getEventType()    { return eventType; }
    public void setEventType(AuditEventType e) { this.eventType = e; }

    public String getDescription()          { return description; }
    public void setDescription(String d)    { this.description = d; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
