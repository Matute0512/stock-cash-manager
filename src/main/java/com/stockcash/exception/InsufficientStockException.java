package com.stockcash.exception;

/**
 * Se lanza cuando se intenta vender más unidades de las disponibles en stock (BR-02).
 */
public class InsufficientStockException extends Exception {

    private final String productName;
    private final int    available;
    private final int    requested;

    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                productName, available, requested));
        this.productName = productName;
        this.available   = available;
        this.requested   = requested;
    }

    public String getProductName() { return productName; }
    public int getAvailable()      { return available; }
    public int getRequested()      { return requested; }
}
