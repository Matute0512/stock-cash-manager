package com.stockcash.security;

import com.stockcash.model.enums.Role;
import java.util.Arrays;

/**
 * Verificador de permisos RBAC — Defensa en Profundidad (NFR-SEC-06).
 *
 * ¿Por qué verificar en la capa Service y no solo en la UI?
 * Si alguien llama directamente al Service (test, otro módulo, API futura),
 * el control de acceso sigue activo. La UI puede omitir botones, pero el
 * Service SIEMPRE verifica permisos — es la última línea de defensa.
 */
public final class PermissionChecker {

    private PermissionChecker() {}

    /**
     * Lanza SecurityException si el usuario actual no tiene ninguno de los roles requeridos.
     * Uso: PermissionChecker.require(Role.ADMIN, Role.SUPERVISOR);
     */
    public static void require(Role... allowedRoles) {
        if (!hasRole(allowedRoles)) {
            String roles = Arrays.stream(allowedRoles)
                .map(Role::getDisplayName)
                .reduce((a, b) -> a + " o " + b)
                .orElse("desconocido");
            throw new SecurityException(
                "Acceso denegado. Se requiere rol: " + roles);
        }
    }

    /** True si el usuario activo tiene alguno de los roles especificados. */
    public static boolean hasRole(Role... allowedRoles) {
        if (!UserSession.isActive()) return false;
        Role current = UserSession.getInstance().getRole();
        for (Role allowed : allowedRoles) {
            if (current == allowed) return true;
        }
        return false;
    }

    public static boolean isAdmin()             { return hasRole(Role.ADMIN); }
    public static boolean isSupervisorOrAbove() { return hasRole(Role.ADMIN, Role.SUPERVISOR); }
    public static boolean isCajeroOrAbove()     { return hasRole(Role.ADMIN, Role.SUPERVISOR, Role.CAJERO); }
}
