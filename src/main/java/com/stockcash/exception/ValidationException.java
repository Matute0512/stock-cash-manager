package com.stockcash.exception;

/**
 * Se lanza cuando una entrada no pasa la validación de InputValidator (NFR-SEC-03).
 * Es RuntimeException porque representa un error de programación
 * si el Controller no validó antes de llamar al Service.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
