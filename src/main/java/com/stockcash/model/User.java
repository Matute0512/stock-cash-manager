package com.stockcash.model;

import com.stockcash.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un usuario del sistema.
 * NO contiene lógica de negocio — esa va en UserService.
 * NO accede a la BD — eso va en UserRepository.
 */
public class User {

    private Long          id;
    private String        username;
    private String        passwordHash;   // Siempre hash BCrypt, nunca texto plano
    private String        fullName;
    private Role          role;
    private boolean       active;
    private int           failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(Long id, String username, String passwordHash, String fullName,
                Role role, boolean active, int failedAttempts, LocalDateTime lockedUntil) {
        this.id             = id;
        this.username       = username;
        this.passwordHash   = passwordHash;
        this.fullName       = fullName;
        this.role           = role;
        this.active         = active;
        this.failedAttempts = failedAttempts;
        this.lockedUntil    = lockedUntil;
    }

    /** True si la cuenta está temporalmente bloqueada (FR-AUTH-03). */
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public static final int MAX_FAILED_ATTEMPTS  = 5;
    public static final int LOCKOUT_MINUTES      = 15;

    // ── Getters y Setters ────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getUsername()                  { return username; }
    public void setUsername(String u)            { this.username = u; }

    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String h)        { this.passwordHash = h; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String n)            { this.fullName = n; }

    public Role getRole()                        { return role; }
    public void setRole(Role r)                  { this.role = r; }

    public boolean isActive()                    { return active; }
    public void setActive(boolean a)             { this.active = a; }

    public int getFailedAttempts()               { return failedAttempts; }
    public void setFailedAttempts(int f)         { this.failedAttempts = f; }

    public LocalDateTime getLockedUntil()        { return lockedUntil; }
    public void setLockedUntil(LocalDateTime l)  { this.lockedUntil = l; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime c)    { this.createdAt = c; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime u)    { this.updatedAt = u; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
