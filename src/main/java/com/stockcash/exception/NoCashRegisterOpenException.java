package com.stockcash.exception;

/**
 * Se lanza cuando se intenta realizar una venta sin caja activa (BR-06).
 */
public class NoCashRegisterOpenException extends Exception {
    public NoCashRegisterOpenException() {
        super("No hay una caja abierta. Abra la caja antes de realizar ventas.");
    }
}
