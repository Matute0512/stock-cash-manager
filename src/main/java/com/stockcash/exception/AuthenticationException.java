package com.stockcash.exception;

/**
 * Se lanza cuando las credenciales de login son incorrectas.
 * Contiene el número de intentos restantes para informar al usuario.
 */
public class AuthenticationException extends Exception {

    private final int remainingAttempts;

    public AuthenticationException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() { return remainingAttempts; }
}
