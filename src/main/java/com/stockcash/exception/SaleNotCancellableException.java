package com.stockcash.exception;

/**
 * Se lanza cuando se intenta anular una venta que no puede cancelarse.
 * Motivos: ya está cancelada, o es de un día anterior (BR-08).
 */
public class SaleNotCancellableException extends Exception {
    public SaleNotCancellableException(String reason) {
        super(reason);
    }
}
