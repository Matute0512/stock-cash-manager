package com.stockcash.exception;

/**
 * Se lanza cuando el usuario existe pero está marcado como inactivo (FR-USR-03).
 */
public class AccountInactiveException extends Exception {
    public AccountInactiveException() {
        super("La cuenta de usuario está desactivada.");
    }
}
