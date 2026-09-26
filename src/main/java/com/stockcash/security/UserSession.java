package com.stockcash.security;

import com.stockcash.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Sesión del usuario autenticado — Patrón Session Object + Singleton.
 *
 * ¿Por qué Singleton?
 * Solo puede existir UNA sesión activa a la vez (FR-AUTH-08).
 * Los datos de sesión viven EXCLUSIVAMENTE en memoria (NFR-SEC-05).
 * Al hacer destroy(), la instancia se anula — no queda rastro en disco.
 *
 * ¿Por qué inmutable tras crearse?
 * Una vez que el usuario se autentica, su rol y datos no deben cambiar
 * durante la sesión. Si se cambia el rol, la sesión debe destruirse y recrearse.
 */
public final class UserSession {

    private static volatile UserSession instance;

    private final Long          userId;
    private final String        username;
    private final String        fullName;
    private final Role          role;
    private final LocalDateTime loginTime;

    private UserSession(Long userId, String username, String fullName, Role role) {
        this.userId    = userId;
        this.username  = username;
        this.fullName  = fullName;
        this.role      = role;
        this.loginTime = LocalDateTime.now();
    }

    /** Crea y almacena la sesión del usuario recién autenticado. */
    public static UserSession create(Long userId, String username, String fullName, Role role) {
        synchronized (UserSession.class) {
            instance = new UserSession(userId, username, fullName, role);
            return instance;
        }
    }

    /** Retorna la sesión activa, o null si no hay ninguna. */
    public static UserSession getInstance() { return instance; }

    /** True si hay una sesión activa en memoria. */
    public static boolean isActive() { return instance != null; }

    /**
     * Destruye la sesión — elimina todos los datos de memoria.
     * Llamar siempre en logout y al cerrar la aplicación (FR-AUTH-06).
     */
    public static void destroy() {
        synchronized (UserSession.class) {
            instance = null;
        }
    }

    // Getters — sin setters: la sesión es inmutable tras crearse
    public Long          getUserId()    { return userId; }
    public String        getUsername()  { return username; }
    public String        getFullName()  { return fullName; }
    public Role          getRole()      { return role; }
    public LocalDateTime getLoginTime() { return loginTime; }
}
