package com.stockcash.exception;

import java.time.LocalDateTime;

/**
 * Se lanza cuando la cuenta está bloqueada por exceso de intentos fallidos (FR-AUTH-03).
 */
public class AccountLockedException extends Exception {

    private final LocalDateTime lockedUntil;

    public AccountLockedException(LocalDateTime lockedUntil) {
        super("Cuenta bloqueada por intentos fallidos.");
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
}
