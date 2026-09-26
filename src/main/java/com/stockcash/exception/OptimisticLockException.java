package com.stockcash.exception;

/**
 * Se lanza cuando el stock fue modificado por otro hilo entre la lectura
 * y la escritura (NFR-REL-04 — Optimistic Locking).
 */
public class OptimisticLockException extends Exception {

    private final String productName;

    public OptimisticLockException(String productName) {
        super(String.format(
            "El stock de '%s' fue modificado por otra operación simultánea. Por favor reintente.",
            productName));
        this.productName = productName;
    }

    public String getProductName() { return productName; }
}
