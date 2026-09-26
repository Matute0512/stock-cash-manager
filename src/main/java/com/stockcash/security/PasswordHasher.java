package com.stockcash.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Wrapper centralizado para operaciones BCrypt (NFR-SEC-01).
 *
 * Nadie en el sistema llama a BCrypt directamente — siempre pasan por aquí.
 * Esto permite cambiar el algoritmo en el futuro sin tocar ningún otro archivo.
 *
 * ¿Por qué BCrypt y no SHA-256/MD5?
 * BCrypt tiene un cost factor configurable que lo hace intencionalmente lento.
 * SHA-256 puede hashearse miles de millones de veces por segundo en una GPU.
 * Con cost=12, BCrypt tarda ~250ms — perfecto para login, imposible para brute-force.
 */
public final class PasswordHasher {

    /** Factor de costo BCrypt — cada incremento duplica el tiempo de cómputo. */
    private static final int COST_FACTOR = 12;

    private PasswordHasher() {}

    /**
     * Genera un hash BCrypt de una contraseña en texto plano.
     * El salt aleatorio se incluye en el hash resultante — no necesita almacenarse separado.
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST_FACTOR));
    }

    /**
     * Verifica si una contraseña en texto plano corresponde al hash almacenado.
     * Nunca comparar strings directamente — BCrypt incluye salt en el hash.
     *
     * @param plainPassword Contraseña ingresada por el usuario
     * @param storedHash    Hash almacenado en la BD (empieza con '$')
     * @return true si la contraseña es correcta
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            // Hash malformado — tratar como verificación fallida
            return false;
        }
    }

    /** Verifica si un hash es el placeholder del seed inicial (no es un hash BCrypt real). */
    public static boolean isPlaceholder(String hash) {
        return hash != null && !hash.startsWith("\\$") && !hash.startsWith("\\$");
    }
}
